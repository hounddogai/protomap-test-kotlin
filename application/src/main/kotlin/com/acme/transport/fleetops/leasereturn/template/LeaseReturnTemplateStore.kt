package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return templates by code. */
interface LeaseReturnTemplateStore {
    fun find(code: String): LeaseReturnTemplate?
    fun save(item: LeaseReturnTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnTemplateStore(
    var items: MutableMap<String, LeaseReturnTemplate>,
    var order: MutableList<String>,
) : LeaseReturnTemplateStore {
    override fun find(code: String): LeaseReturnTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnTemplate) {
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

    override fun list(): MutableList<LeaseReturnTemplate> {
        val out = mutableListOf<LeaseReturnTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnTemplateStore(): LeaseReturnTemplateStore {
    return MemoryLeaseReturnTemplateStore(mutableMapOf<String, LeaseReturnTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnTemplateStore(
    var inner: LeaseReturnTemplateStore,
    var cache: TtlCache<LeaseReturnTemplate>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnTemplateStore {
    override fun find(code: String): LeaseReturnTemplate? {
        val hit = (cache.get(code) as LeaseReturnTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnTemplate> = inner.list()
}

fun newCachedLeaseReturnTemplateStore(inner: LeaseReturnTemplateStore, ttl: Int, clock: Clock): LeaseReturnTemplateStore {
    return CachedLeaseReturnTemplateStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnTemplate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return templates. Empty fields match all. */
data class LeaseReturnTemplateQuery(
    var category: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnTemplate): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LeaseReturnTemplate>): MutableList<LeaseReturnTemplate> {
        val matched = filter(items) { candidate: LeaseReturnTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnTemplates(store: LeaseReturnTemplateStore, query: LeaseReturnTemplateQuery): MutableList<LeaseReturnTemplate> {
    return query.apply(store.list())
}
