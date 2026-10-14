package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default transit time exceptions by code. */
interface DefaultTransitTimeExceptionStore {
    fun find(code: String): DefaultTransitTimeException?
    fun save(item: DefaultTransitTimeException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultTransitTimeException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultTransitTimeExceptionStore(
    var items: MutableMap<String, DefaultTransitTimeException>,
    var order: MutableList<String>,
) : DefaultTransitTimeExceptionStore {
    override fun find(code: String): DefaultTransitTimeException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultTransitTimeException) {
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

    override fun list(): MutableList<DefaultTransitTimeException> {
        val out = mutableListOf<DefaultTransitTimeException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultTransitTimeExceptionStore(): DefaultTransitTimeExceptionStore {
    return MemoryDefaultTransitTimeExceptionStore(
        mutableMapOf<String, DefaultTransitTimeException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultTransitTimeExceptionStore(
    var inner: DefaultTransitTimeExceptionStore,
    var cache: TtlCache<DefaultTransitTimeException>,
    var hits: Int,
    var misses: Int,
) : DefaultTransitTimeExceptionStore {
    override fun find(code: String): DefaultTransitTimeException? {
        val hit = (cache.get(code) as DefaultTransitTimeException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultTransitTimeException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultTransitTimeException> = inner.list()
}

fun newCachedDefaultTransitTimeExceptionStore(inner: DefaultTransitTimeExceptionStore, ttl: Int, clock: Clock): DefaultTransitTimeExceptionStore {
    return CachedDefaultTransitTimeExceptionStore(
        inner = inner,
        cache = newTtlCache<DefaultTransitTimeException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default transit time exceptions. Empty fields match all. */
data class DefaultTransitTimeExceptionQuery(
    var unit: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultTransitTimeException): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultTransitTimeException>): MutableList<DefaultTransitTimeException> {
        val matched = filter(items) { candidate: DefaultTransitTimeException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultTransitTimeExceptions(store: DefaultTransitTimeExceptionStore, query: DefaultTransitTimeExceptionQuery): MutableList<DefaultTransitTimeException> {
    return query.apply(store.list())
}
