package com.github.xtranslation.core.manager;


import com.github.xtranslation.core.core.TransClassMeta;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TransClassMetaCacheManager: TransClassMeta缓存管理器
 * <p>
 * 该类是翻译框架的元数据缓存中心，负责缓存和管理TransClassMeta对象，以提高系统性能。
 * 采用单例模式和线程安全的ConcurrentHashMap实现，确保在高并发环境下的数据一致性。
 * </p>
 * <p>
 * 核心设计要点：
 * 1. 性能优化：避免重复解析相同的类元数据，通过缓存机制大幅提升处理速度
 * 2. 内存效率：只缓存需要翻译的类（needTrans()返回true），避免无意义的内存占用
 * 3. 线程安全：使用ConcurrentHashMap确保多线程环境下的安全访问
 * 4. 懒加载：按需创建和缓存TransClassMeta对象，避免启动时的大量内存占用
 * </p>
 * <p>
 * (运行时修改类结构的情况极其罕见,所以永驻缓存问题不大,然后就是也可以优化,例如手动清理接口,加入监控系统等等)
 * 经过综合以下考虑,就认为这个类是合适的，因为:
 * 1符合使用场景：类元数据在运行时基本不会变化
 * 2性能优秀：避免了复杂的缓存管理开销
 * 3简单可靠：代码简单，不容易出错
 * 4易于理解：开发者容易理解和维护
 *
 * @author zhangxiaoxiang
 * @since 2025/7/27
 */
public class TransClassMetaCacheManager implements Serializable {

    private static final long serialVersionUID = 3076627700677041940L;

    /**
     * TransClassMeta对象缓存池
     * <p>
     * 使用线程安全的ConcurrentHashMap存储类名到TransClassMeta对象的映射关系。
     * Key为类的全限定名，Value为对应的TransClassMeta对象。
     * 采用懒加载策略，只有在实际需要时才创建和缓存对象。
     * </p>
     */
    private static final Map<String, TransClassMeta> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取指定类的TransClassMeta对象
     * <p>
     * 这是缓存管理器的核心方法，实现了高效的元数据获取机制：
     * 1. 首先从缓存中查找，如果存在则直接返回（快速路径）
     * 2. 如果缓存中不存在，则创建新的TransClassMeta对象
     * 3. 只有当该类确实需要翻译时（needTrans()返回true）才进行缓存
     * 4. 返回对应的TransClassMeta对象
     * </p>
     * <p>
     * 性能优化策略：
     * - 避免缓存不需要翻译的类，节省内存空间
     * - 通过类名作为键值，确保唯一性和快速查找
     * - 使用ConcurrentHashMap保证线程安全
     * </p>
     *
     * @param clazz 需要获取TransClassMeta的类
     * @return 对应的TransClassMeta对象
     */
    public static TransClassMeta getTransClassMeta(Class<?> clazz) {
        // 1. 尝试从缓存中获取已存在的TransClassMeta对象
        TransClassMeta temp = CACHE.get(clazz.getName());

        // 2. 如果缓存中不存在该类的元数据
        if (null == temp) {
            // 3. 创建新的TransClassMeta对象并解析类的元数据
            temp = new TransClassMeta(clazz);

            // 4. 只缓存需要翻译的类，避免无意义的内存占用
            // 这是一个重要的优化点：不需要翻译的类不会被缓存
            if (temp.needTrans()) {
                CACHE.put(clazz.getName(), temp);
            }
        }

        // 5. 返回TransClassMeta对象（无论是从缓存获取还是新创建的）
        return temp;
    }
}
