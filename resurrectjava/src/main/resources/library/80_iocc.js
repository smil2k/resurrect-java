function FindCertificatesByName(cn) {
    var x = factory.findAll("com.lhsystems.iocc.security.common.X509Authentication");

    var res=new ArrayList();
    for ( var i in x ) {
        if(x[i].certificate.info.subject.contains(cn)) {
            res.add(x[i]);
        }
    }
    return res;
}

function ListCertificates(list) {
    logger.printf("%12s | %46s | %15s | %26s | %26s\n", "Object#", "Name", "Serial", "Valid from", "Valid Until");
    for (var i =0 ; i< list.length ; i++) {
        var itm=list[i];
        var info=itm.certificate.info;
        logger.printf("%12d | %46s | %15s | %25s | %25s\n", itm.getObjectId().getObjectId(), info.subject, info.serialNum,
        formatTimeUnixMs(info.interval.notBefore), formatTimeUnixMs(info.interval.notAfter) );
    }
}