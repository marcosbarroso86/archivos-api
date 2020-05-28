package ar.com.opendevsolutions.archivos.service;

import ar.com.opendevsolutions.archivos.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
public class ArchivosService {

    @Value("${files.uploadDir}")
    String directorio;

    @Value("${fecha.maxima.liquidacion}")
    Integer fechaMaximaLiquidacion;

    private Path rutaDirectorio;

    ArchivosService() {
    }

    public Path getRutaDirectorio() {
        if (this.rutaDirectorio == null) {
            this.rutaDirectorio = Paths.get(directorio).toAbsolutePath().normalize();

            try {
                Files.createDirectories(this.rutaDirectorio);
            } catch (Exception ex) {
                throw new ValidationException("No se puede utilizar la ruta del directorio ", ex);
            }
            return this.rutaDirectorio;
        } else {
            return this.rutaDirectorio;
        }
    }

    public String guardarArchivo(MultipartFile file, String prestador) {
        LocalDateTime now = LocalDateTime.now();
        String periodo = String.valueOf(now.getYear()) + String.valueOf(now.getMonthValue());
        if(now.getDayOfMonth() > fechaMaximaLiquidacion) {
            throw new ValidationException("Ya no esta permitido enviar liquidaciones para este periodo");
        } else {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            try {
                if (fileName.contains("..")) {
                    throw new ValidationException("Error, el nombre del archivo no es admisible " + fileName);
                }
                if (Files.notExists(getRutaDirectorio().resolve(prestador))) {
                    Files.createDirectory(getRutaDirectorio().resolve(prestador));
                }
                if (Files.notExists(getRutaDirectorio().resolve(prestador + "/" + periodo))) {
                    Files.createDirectory(getRutaDirectorio().resolve(prestador + "/" + periodo));
                }
                Files.copy(file.getInputStream(), getRutaDirectorio().resolve(prestador + "/" + periodo).resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                return fileName;
            } catch (IOException ex) {
                throw new ValidationException("No se pudo guardar el archivo " + fileName + " , intente nuevamente", ex);
            }
        }

    }

    public Resource cargarArchivo(String fileName) {
        try {
            Path filePath = getRutaDirectorio().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ValidationException("Archivo no existente " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ValidationException("Archivo no existente " + fileName, ex);
        }
    }


    public List<String> listarArchivos(String prestador, String periodo) throws IOException {
        List<String> result = new LinkedList<>();
        if (Files.exists(getRutaDirectorio().resolve(prestador).resolve(periodo))) {
            try (Stream<Path> paths = Files.walk(Paths.get(getRutaDirectorio().resolve(prestador).resolve(periodo).toString()))) {
                paths.filter(Files::isRegularFile).forEach(x -> result.add(x.getFileName().toString()));
            }
        }
        return result;
    }

    public List<String> listarPrestadores() throws IOException {
        File file = new File(directorio);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        System.out.println(Arrays.toString(directories));
        return Arrays.asList(directories);
    }

    public void borrarArchivo(String archivo, String prestador, String periodo) throws IOException {
        LocalDateTime now = LocalDateTime.now();
        if(now.getDayOfMonth() > fechaMaximaLiquidacion) {
            throw new ValidationException("Ya no esta permitido eliminar liquidaciones para este periodo");
        } else {
            Files.deleteIfExists(Paths.get(getRutaDirectorio().resolve(prestador).resolve(periodo).resolve(archivo).toString()));
        }
    }

}
