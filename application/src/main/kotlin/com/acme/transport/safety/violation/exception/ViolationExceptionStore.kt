package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation exceptions by code. */
interface ViolationExceptionStore {
    fun find(code: String): ViolationException?
    fun save(item: ViolationException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationExceptionStore(
    var items: MutableMap<String, ViolationException>,
    var order: MutableList<String>,
) : ViolationExceptionStore {
    override fun find(code: String): ViolationException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationException) {
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

    override fun list(): MutableList<ViolationException> {
        val out = mutableListOf<ViolationException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationExceptionStore(): ViolationExceptionStore {
    return MemoryViolationExceptionStore(mutableMapOf<String, ViolationException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationExceptionStore(
    var inner: ViolationExceptionStore,
    var cache: TtlCache<ViolationException>,
    var hits: Int,
    var misses: Int,
) : ViolationExceptionStore {
    override fun find(code: String): ViolationException? {
        val hit = (cache.get(code) as ViolationException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationException> = inner.list()
}

fun newCachedViolationExceptionStore(inner: ViolationExceptionStore, ttl: Int, clock: Clock): ViolationExceptionStore {
    return CachedViolationExceptionStore(
        inner = inner,
        cache = newTtlCache<ViolationException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation exceptions. Empty fields match all. */
data class ViolationExceptionQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationException>): MutableList<ViolationException> {
        val matched = filter(items) { candidate: ViolationException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationExceptions(store: ViolationExceptionStore, query: ViolationExceptionQuery): MutableList<ViolationException> {
    return query.apply(store.list())
}
