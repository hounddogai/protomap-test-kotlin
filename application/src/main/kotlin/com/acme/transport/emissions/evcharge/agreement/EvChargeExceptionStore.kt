package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge exceptions by code. */
interface EvChargeExceptionStore {
    fun find(code: String): EvChargeException?
    fun save(item: EvChargeException)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeExceptionStore(
    var items: MutableMap<String, EvChargeException>,
    var order: MutableList<String>,
) : EvChargeExceptionStore {
    override fun find(code: String): EvChargeException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeException) {
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

    override fun list(): MutableList<EvChargeException> {
        val out = mutableListOf<EvChargeException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeExceptionStore(): EvChargeExceptionStore {
    return MemoryEvChargeExceptionStore(mutableMapOf<String, EvChargeException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeExceptionStore(
    var inner: EvChargeExceptionStore,
    var cache: TtlCache<EvChargeException>,
    var hits: Int,
    var misses: Int,
) : EvChargeExceptionStore {
    override fun find(code: String): EvChargeException? {
        val hit = (cache.get(code) as EvChargeException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeException> = inner.list()
}

fun newCachedEvChargeExceptionStore(inner: EvChargeExceptionStore, ttl: Int, clock: Clock): EvChargeExceptionStore {
    return CachedEvChargeExceptionStore(
        inner = inner,
        cache = newTtlCache<EvChargeException>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge exceptions. Empty fields match all. */
data class EvChargeExceptionQuery(
    var label: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeException): Boolean {
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

    fun apply(items: MutableList<EvChargeException>): MutableList<EvChargeException> {
        val matched = filter(items) { candidate: EvChargeException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeExceptions(store: EvChargeExceptionStore, query: EvChargeExceptionQuery): MutableList<EvChargeException> {
    return query.apply(store.list())
}
