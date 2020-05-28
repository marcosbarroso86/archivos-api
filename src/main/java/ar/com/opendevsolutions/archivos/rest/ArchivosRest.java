package ar.com.opendevsolutions.archivos.rest;

import ar.com.opendevsolutions.archivos.exception.ValidationException;
import ar.com.opendevsolutions.archivos.service.ArchivosService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@Api(value="/",produces ="application/json")
public class ArchivosRest {

    @Autowired
    ArchivosService archivosService;

    @ApiOperation(value = "Guardar archivo", response = UploadFileResponse.class)
    @PostMapping("/file/{prestador:.+}")
    public UploadFileResponse uploadFile(@PathVariable(value = "prestador") String prestador, @RequestParam("file") MultipartFile file) {
        String fileName = archivosService.guardarArchivo(file, prestador);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/file/" + prestador + "/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @ApiOperation(value = "Obtener archivo", response = Resource.class)
    @GetMapping("/file/{prestador:.+}/{periodo:.+}/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable(value = "prestador") String prestador, @PathVariable(value = "periodo") String periodo, @PathVariable String fileName, HttpServletRequest request) {
        Resource resource = archivosService.cargarArchivo(prestador + "/" + periodo + "/" + fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            new ValidationException("No se pudo determinar el tipo");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @ApiOperation(value = "Obtener archivos por proceso", response = List.class)
    @GetMapping("/file/{prestador:.+}/{periodo:.+}")
    public List<String> listFiles(@PathVariable(value = "prestador") String prestador, @PathVariable(value = "periodo") String periodo) throws IOException {
        return archivosService.listarArchivos(prestador, periodo);
    }

    @ApiOperation(value = "Obtener prestadores", response = List.class)
    @GetMapping("/file")
    public List<String> listFiles() throws IOException {
        return archivosService.listarPrestadores();
    }

    @ApiOperation(value = "Eliminar archivo")
    @DeleteMapping("/file/{prestador:.+}/{periodo:.+}/{fileName:.+}")
    public ResponseEntity deleteFile(@PathVariable(value = "prestador") String prestador, @PathVariable(value = "periodo") String periodo, @PathVariable(value = "fileName") String fileName) throws IOException {
        archivosService.borrarArchivo(fileName, prestador, periodo);
        return ResponseEntity.accepted().build();
    }

}
