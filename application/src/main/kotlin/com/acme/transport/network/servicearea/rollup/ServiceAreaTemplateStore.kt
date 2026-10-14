package com.acme.transport.network.servicearea.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area templates by code. */
interface ServiceAreaTemplateStore {
    fun find(code: String): ServiceAreaTemplate?
    fun save(item: ServiceAreaTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaTemplateStore(
    var items: MutableMap<String, ServiceAreaTemplate>,
    var order: MutableList<String>,
) : ServiceAreaTemplateStore {
    override fun find(code: String): ServiceAreaTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaTemplate) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ServiceAreaTemplate> {
        val out = mutableListOf<ServiceAreaTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaTemplateStore(): ServiceAreaTemplateStore {
    return MemoryServiceAreaTemplateStore(mutableMapOf<String, ServiceAreaTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaTemplateStore(
    var inner: ServiceAreaTemplateStore,
    var cache: TtlCache<ServiceAreaTemplate>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaTemplateStore {
    override fun find(code: String): ServiceAreaTemplate? {
        val hit = (cache.get(code) as ServiceAreaTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaTemplate> = inner.list()
}

fun newCachedServiceAreaTemplateStore(inner: ServiceAreaTemplateStore, ttl: Int, clock: Clock): ServiceAreaTemplateStore {
    return CachedServiceAreaTemplateStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area templates. Empty fields match all. */
data class ServiceAreaTemplateQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaTemplate>): MutableList<ServiceAreaTemplate> {
        val matched = filter(items) { candidate: ServiceAreaTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaTemplates(store: ServiceAreaTemplateStore, query: ServiceAreaTemplateQuery): MutableList<ServiceAreaTemplate> {
    return query.apply(store.list())
}
