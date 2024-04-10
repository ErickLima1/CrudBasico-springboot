package com.example.springboot.controllers;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductsModel;
import com.example.springboot.repositories.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @PostMapping("/produtos")
    public ResponseEntity<ProductsModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        var productModel = new ProductsModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/produtos")
    public ResponseEntity<List<ProductsModel>> getAllProducts() {
        List<ProductsModel> productsList = productRepository.findAll();

        if(!productsList.isEmpty()) {
            for(ProductsModel product : productsList) {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.findAll());

    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductsModel> productO = productRepository.findById(id);

        return productO.<ResponseEntity<Object>>map(productsModel -> ResponseEntity.status(HttpStatus.OK).body(productsModel))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Produto Não Encontrado"));
    }

    @PutMapping("/produtos/{id}") //falta testar    
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto) {
        Optional<ProductsModel> productO = productRepository.findById(id);

        if (productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto Não Encontrado");
        }

        var productModel = productO.get();
        BeanUtils.copyProperties(productRecordDto, productModel); //Fazendo conversão do DTO para Model
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductsModel> productO = productRepository.findById(id);

        if (productO.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto Não Existe");
        }
        productRepository.delete(productO.get());
        return ResponseEntity.status(HttpStatus.OK).body("Produto Deletado COm Sucesso !");
    }
}
