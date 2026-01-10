package com.hksc.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hksc.common.result.Result;
import com.hksc.product.dto.ProductDTO;
import com.hksc.product.dto.ProductQueryDTO;
import com.hksc.product.service.ProductService;
import com.hksc.product.vo.ProductDetailVO;
import com.hksc.product.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 商品列表查询（返回VO）
     */
    @GetMapping("/list")
    public Result<Page<ProductVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(required = false) String brand,
                                        @RequestParam(required = false) Integer isSeckill) {
        // 构建查询DTO
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setLimit(limit);
        queryDTO.setKeyword(keyword);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setBrand(brand);
        queryDTO.setIsSeckill(isSeckill);

        Page<ProductVO> result = productService.getProductListWithStock(queryDTO);
        return Result.success(result);
    }

    /**
     * 商品详情（返回VO，用于前端展示）
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        ProductDetailVO product = productService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 获取商品DTO（用于Feign调用）
     */
    @GetMapping("/dto/{id}")
    public Result<ProductDTO> getProductDTO(@PathVariable Long id) {
        ProductDTO product = productService.getProductDTO(id);
        return Result.success(product);
    }

    /**
     * 扣减库存（内部调用）
     */
    @PostMapping("/deduct")
    public Result<Boolean> deductStock(@RequestParam Long productId, @RequestParam Integer count) {
        return Result.success(productService.doDeduction(productId, count));
    }

    /**
     * 恢复库存（内部调用）
     */
    @PostMapping("/restore")
    public Result<Boolean> restoreStock(@RequestParam Long productId, @RequestParam Integer count) {
        productService.restoreStock(productId, count);
        return Result.success(true);
    }
}

