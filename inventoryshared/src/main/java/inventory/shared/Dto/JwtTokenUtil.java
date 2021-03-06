package inventory.shared.Dto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/*
    Our simple static class that demonstrates how to create and decode JWTs.
 */
public class JwtTokenUtil {

	// The secret key. This should be in a property file NOT under source
	// control and not hard coded in real life. We're putting it here for
	// simplicity.
	private static String SECRET_KEY =
			"oeRaYY7Wo24sDqKSX3IM9ASGmdGPmkTd9jo1QTy4b7P9Ze5_9hKolVX8xNrQDcNRfVEdTZNOuOyqEGhXEbdJI" +
					"-ZQ19k_o9MI0y3eZN2lp9jow55FfXMiINEdt1XR85VipRLSOkT6kSpzs2x-jbLDiz9iFVzkd81YKxMgPA7VfZeQUm4n" +
					"-mOmnWMaVX30zGFU4L3oPBctYKkl4dYfqYWqRNfrgPJVi5DGFjywgxx0ASEiJHtV72paI3fDR2XwlSkyhhmY" +
					"-ICjCRmsJN4fX1pdoL8a18-aQrvyu4j0Os6dVPYIoPvvY0SAZtWYKHfM15g7A3HD4cVREf9cUsprCRK93w";
	private static String SECRET_KEY_REFRESH =
			"jRxYG4M1IsWeyLKx6rivSNpdgm3jqGinNuWHqavlYgxxvyo11U5Vf4Yx0U2oT9gme0KfSZEhrEfBPJYz79o5C9RXa4gp2eeHi4cJmND8JYmb3G0Xpg4Vq2HnJnidt5QcuWQUjKZ2JAIdzfGUdGi2geibDhm7rXPc1cLz9QEWOM3rQsYNIuhN5sCSLfAkPpufVqawrltFUKIzMmOt5tGD6VxunKBxPLXQvXCaH9xEng06vLlynv37ZbWNnzMuZgDUThgn7aL9YEkQhFhfMqTy7E7OIgDY5Ffp04xjNZsccZOfNXy1Yt5HnsYIJ2XicPdW0E8EdzHZ66Jho65fnVZywg085QqkOdRWSyDlLNEhfwravaNXF1LfdEI5so";

	//Sample method to construct a JWT
	public static String createJWT(String issuer, String subject, long ttlMillis, boolean refresh) {

		//The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		//We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes;
		if (refresh)
			apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY_REFRESH);
		else
			apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

		//Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setIssuedAt(now).setSubject(subject).setIssuer(issuer)
				.signWith(signatureAlgorithm, signingKey);

		//if it has been specified, let's add the expiration
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		//Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

	public static Claims decodeJWT(String jwt, boolean refresh) {

		//This line will throw an exception if it is not a signed JWS (as expected)
		if (refresh)
			return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY_REFRESH))
					.parseClaimsJws(jwt).getBody();

		return Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY)).parseClaimsJws(jwt)
				.getBody();
	}

}
