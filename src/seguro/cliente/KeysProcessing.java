package seguro.cliente;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.asn1.x500.X500Name;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class KeysProcessing
{
    public static KeyPair keyPairGenerator() throws NoSuchAlgorithmException
    {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA");
        kpGen.initialize(1024, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    public static X509Certificate generateCertificate(KeyPair keyPair) throws OperatorCreationException, CertificateException {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.add(1, 10);
        X509v3CertificateBuilder x509v3CertificateBuilder = new X509v3CertificateBuilder(new X500Name("CN=localhost"),
                BigInteger.valueOf(1L),
                Calendar.getInstance().getTime(),
                endCalendar.getTime(),
                new X500Name("CN=localhost"),
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
        ContentSigner contentSigner = (new JcaContentSignerBuilder("SHA1withRSA")).build(keyPair.getPrivate());
        X509CertificateHolder x509CertificateHolder = x509v3CertificateBuilder.build(contentSigner);
        return (new JcaX509CertificateConverter()).setProvider("BC").getCertificate(x509CertificateHolder);
    }
}