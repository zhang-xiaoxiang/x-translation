package com.github.xtranslation.core.resolver;


/**
 * TransObjResolver: 解析包装对象，获取需要翻译的对象 (后期改名 WrapperObjectResolver)
 *
 * <p>设计说明: 为什么没有采用"约定优于配置"的方式</p>
 * <p>1. 包装类型有限: 常见的泛型包装类如 {@code Result<T>}、{@code Result<Page<T>>}、{@code ServiceResult<T>} 等类型数量有限，
 * 用户只需实现一次即可长期使用，配置成本低。</p>
 * <p>2. 注解方式成本高: 如果采用注解标记的方式，虽然可以减少用户配置工作，但会带来额外的反射调用开销。
 * 每次翻译都需要通过反射检查注解，性能损耗较大，得不偿失。</p>
 * <p>3. 综合考虑: 基于使用频率低、配置一次长期受益、避免性能损耗等因素，最终决定采用当前接口方式，
 * 由用户显式实现解析逻辑。</p>
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public interface TransObjResolver {

    /**
     * 判断是否支持此对象
     *
     * @param obj 原包装对象
     * @return 是否支持此对象
     */
    boolean support(Object obj);

    /**
     * 解析包装对象，获取需要翻译的对象
     *
     * @param obj 原包装对象 如:{@code Result<T>}中的data字段值
     * @return 需要翻译的对象 如:UserVO
     */
    Object resolveTransObj(Object obj);

}
