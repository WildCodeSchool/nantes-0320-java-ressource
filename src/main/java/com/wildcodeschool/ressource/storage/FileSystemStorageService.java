package com.wildcodeschool.ressource.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service used for interacting with the storage
 * implements StorageService
 */
@Service
public class FileSystemStorageService implements StorageService {

    private final List<Path> rootLocation = new ArrayList<>();

    /**
     * Define how the Spring dependency injector needs to create the FileSystemStorageService
     * It will inject a StorageProperties instance to the constructor
     *
     * @param properties the StorageProperties that offers access to the application.properties storage values
     */
    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation.add(Paths.get(properties.getLocationCompany()));
        this.rootLocation.add(Paths.get(properties.getLocationProducts()));
    }

    /**
     * Store a file in the storage.location directory
     *
     * @param file MultipartFile to store
     * @throws StorageException if anythong goes wrong (Empty file, filesystem unable to store...)
     */
    @Override
    public void store(MultipartFile file, int indexLocation, String nameFile) {
        try {
            if (!file.isEmpty()) {
                Files.copy(file.getInputStream(), this.rootLocation.get(indexLocation).resolve(nameFile));
            }
            //throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Get the Path for the filename file in the storage.location directory
     *
     * @return Path
     */
    @Override
    public Path load(String filename, int indexLocation) {
        return rootLocation.get(indexLocation).resolve(filename);
    }

    /**
     * Get a Resource object for the filename file in the storage.location directory
     *
     * @return Resource the file
     * @throws StorageFileNotFoundException if the file can't be read
     */
    @Override
    public Resource loadAsResource(String filename, int indexLocation) {
        try {
            Path file = load(filename, indexLocation);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    /**
     * Delete recursively the storage.location directory
     */
    @Override
    public void deleteByName(String fileName, int indexLocation) {

        Path file = load(fileName, indexLocation);
        try {
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                FileSystemUtils.deleteRecursively(file.toFile());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the storage.location directory
     *
     * @throws StorageException if the filesystem can't create the directory
     */
    @Override
    public void init() {
        try {
            for (Path location : rootLocation) {
                if (!Files.exists(location)) {
                    Files.createDirectory(location);
                }
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
