package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect exceptions by code. */
interface DefectExceptionStore {
    fun find(code: String): DefectException?
    fun save(item: DefectException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectExceptionStore(
    var items: MutableMap<String, DefectException>,
    var order: MutableList<String>,
) : DefectExceptionStore {
    override fun find(code: String): DefectException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectException) {
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

    override fun list(): MutableList<DefectException> {
        val out = mutableListOf<DefectException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectExceptionStore(): DefectExceptionStore {
    return MemoryDefectExceptionStore(mutableMapOf<String, DefectException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectExceptionStore(
    var inner: DefectExceptionStore,
    var cache: TtlCache<DefectException>,
    var hits: Int,
    var misses: Int,
) : DefectExceptionStore {
    override fun find(code: String): DefectException? {
        val hit = (cache.get(code) as DefectException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectException> = inner.list()
}

fun newCachedDefectExceptionStore(inner: DefectExceptionStore, ttl: Int, clock: Clock): DefectExceptionStore {
    return CachedDefectExceptionStore(
        inner = inner,
        cache = newTtlCache<DefectException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect exceptions. Empty fields match all. */
data class DefectExceptionQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectException>): MutableList<DefectException> {
        val matched = filter(items) { candidate: DefectException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectExceptions(store: DefectExceptionStore, query: DefectExceptionQuery): MutableList<DefectException> {
    return query.apply(store.list())
}
