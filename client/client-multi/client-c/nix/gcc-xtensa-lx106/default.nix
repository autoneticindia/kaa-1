{ stdenv, fetchurl, zlib
}:
let
  src =
    if stdenv.system == "i686-linux" then
       fetchurl {
         url = "http://arduino.esp8266.com/linux32-xtensa-lx106-elf.tar.gz";
         sha256 = "0nlmvjfjv3mcj0ihvf27dflrmjhads00wr2si42zny00ky0ifj5j";
       } else
    if stdenv.system == "x86_64-linux" then
       fetchurl {
         url = http://arduino.esp8266.com/linux64-xtensa-lx106-elf-gb404fb9.tar.gz;
         sha256 = "1dvk2i1r5nw9ajlv4h4rs2b2149qa0rayz8n4sd8h85kv3xmgw26";
       } else
    if stdenv.system == "i686-darwin" || stdenv.system == "x86_64-darwin" then
       fetchurl {
         url = http://arduino.esp8266.com/osx-xtensa-lx106-elf-gb404fb9-2.tar.gz;
         sha256 = "1rp4p5b9wqddm8gfw6mwax906c0pf54kv7glw1ai7gcp74cm1w8c";
       } else
    abort "no snapshot for this platform (missing target triple)";

in stdenv.mkDerivation {
  name = "gcc-xtensa-lx106-1.20.0-26-gb404fb9-2";

  inherit src;

  installPhase = ''
    mkdir -p $out
    cp -r . $out
  '';

  preFixup = if stdenv.isLinux then let
    rpath = stdenv.lib.concatStringsSep ":" [
      "$out/lib"
      (stdenv.lib.makeLibraryPath [ zlib ])
      "${stdenv.cc.cc}/lib${stdenv.lib.optionalString stdenv.is64bit "64"}"
    ];
  in ''
    find -H $out/ -type f -executable -exec \
      patchelf \
        --interpreter "${stdenv.glibc}/lib/${stdenv.cc.dynamicLinker}" \
        --set-rpath "${rpath}" \
        {} \;
  '' else "";

  dontStrip = true;
}