package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback accessorial templates by code. */
interface FallbackAccessorialTemplateStore {
    fun find(code: String): FallbackAccessorialTemplate?
    fun save(item: FallbackAccessorialTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAccessorialTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAccessorialTemplateStore(
    var items: MutableMap<String, FallbackAccessorialTemplate>,
    var order: MutableList<String>,
) : FallbackAccessorialTemplateStore {
    override fun find(code: String): FallbackAccessorialTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAccessorialTemplate) {
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

    override fun list(): MutableList<FallbackAccessorialTemplate> {
        val out = mutableListOf<FallbackAccessorialTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAccessorialTemplateStore(): FallbackAccessorialTemplateStore {
    return MemoryFallbackAccessorialTemplateStore(
        mutableMapOf<String, FallbackAccessorialTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAccessorialTemplateStore(
    var inner: FallbackAccessorialTemplateStore,
    var cache: TtlCache<FallbackAccessorialTemplate>,
    var hits: Int,
    var misses: Int,
) : FallbackAccessorialTemplateStore {
    override fun find(code: String): FallbackAccessorialTemplate? {
        val hit = (cache.get(code) as FallbackAccessorialTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAccessorialTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAccessorialTemplate> = inner.list()
}

fun newCachedFallbackAccessorialTemplateStore(inner: FallbackAccessorialTemplateStore, ttl: Int, clock: Clock): FallbackAccessorialTemplateStore {
    return CachedFallbackAccessorialTemplateStore(
        inner = inner,
        cache = newTtlCache<FallbackAccessorialTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback accessorial templates. Empty fields match all. */
data class FallbackAccessorialTemplateQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAccessorialTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAccessorialTemplate>): MutableList<FallbackAccessorialTemplate> {
        val matched = filter(items) { candidate: FallbackAccessorialTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAccessorialTemplates(store: FallbackAccessorialTemplateStore, query: FallbackAccessorialTemplateQuery): MutableList<FallbackAccessorialTemplate> {
    return query.apply(store.list())
}
