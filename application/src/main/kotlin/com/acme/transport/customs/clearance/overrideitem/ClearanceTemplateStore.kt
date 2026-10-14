package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance templates by code. */
interface ClearanceTemplateStore {
    fun find(code: String): ClearanceTemplate?
    fun save(item: ClearanceTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceTemplateStore(
    var items: MutableMap<String, ClearanceTemplate>,
    var order: MutableList<String>,
) : ClearanceTemplateStore {
    override fun find(code: String): ClearanceTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceTemplate) {
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

    override fun list(): MutableList<ClearanceTemplate> {
        val out = mutableListOf<ClearanceTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceTemplateStore(): ClearanceTemplateStore {
    return MemoryClearanceTemplateStore(mutableMapOf<String, ClearanceTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceTemplateStore(
    var inner: ClearanceTemplateStore,
    var cache: TtlCache<ClearanceTemplate>,
    var hits: Int,
    var misses: Int,
) : ClearanceTemplateStore {
    override fun find(code: String): ClearanceTemplate? {
        val hit = (cache.get(code) as ClearanceTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceTemplate> = inner.list()
}

fun newCachedClearanceTemplateStore(inner: ClearanceTemplateStore, ttl: Int, clock: Clock): ClearanceTemplateStore {
    return CachedClearanceTemplateStore(
        inner = inner,
        cache = newTtlCache<ClearanceTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance templates. Empty fields match all. */
data class ClearanceTemplateQuery(
    var locale: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceTemplate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceTemplate>): MutableList<ClearanceTemplate> {
        val matched = filter(items) { candidate: ClearanceTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceTemplates(store: ClearanceTemplateStore, query: ClearanceTemplateQuery): MutableList<ClearanceTemplate> {
    return query.apply(store.list())
}
