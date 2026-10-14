package com.acme.transport.safety.incident.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident exceptions by code. */
interface IncidentExceptionStore {
    fun find(code: String): IncidentException?
    fun save(item: IncidentException)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentExceptionStore(
    var items: MutableMap<String, IncidentException>,
    var order: MutableList<String>,
) : IncidentExceptionStore {
    override fun find(code: String): IncidentException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentException) {
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

    override fun list(): MutableList<IncidentException> {
        val out = mutableListOf<IncidentException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentExceptionStore(): IncidentExceptionStore {
    return MemoryIncidentExceptionStore(mutableMapOf<String, IncidentException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentExceptionStore(
    var inner: IncidentExceptionStore,
    var cache: TtlCache<IncidentException>,
    var hits: Int,
    var misses: Int,
) : IncidentExceptionStore {
    override fun find(code: String): IncidentException? {
        val hit = (cache.get(code) as IncidentException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentException> = inner.list()
}

fun newCachedIncidentExceptionStore(inner: IncidentExceptionStore, ttl: Int, clock: Clock): IncidentExceptionStore {
    return CachedIncidentExceptionStore(
        inner = inner,
        cache = newTtlCache<IncidentException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident exceptions. Empty fields match all. */
data class IncidentExceptionQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentException>): MutableList<IncidentException> {
        val matched = filter(items) { candidate: IncidentException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentExceptions(store: IncidentExceptionStore, query: IncidentExceptionQuery): MutableList<IncidentException> {
    return query.apply(store.list())
}
