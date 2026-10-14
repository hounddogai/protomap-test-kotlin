package com.acme.transport.terminal.dwell.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dwell exceptions by code. */
interface DefaultDwellExceptionStore {
    fun find(code: String): DefaultDwellException?
    fun save(item: DefaultDwellException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDwellException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDwellExceptionStore(
    var items: MutableMap<String, DefaultDwellException>,
    var order: MutableList<String>,
) : DefaultDwellExceptionStore {
    override fun find(code: String): DefaultDwellException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDwellException) {
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

    override fun list(): MutableList<DefaultDwellException> {
        val out = mutableListOf<DefaultDwellException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDwellExceptionStore(): DefaultDwellExceptionStore {
    return MemoryDefaultDwellExceptionStore(
        mutableMapOf<String, DefaultDwellException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDwellExceptionStore(
    var inner: DefaultDwellExceptionStore,
    var cache: TtlCache<DefaultDwellException>,
    var hits: Int,
    var misses: Int,
) : DefaultDwellExceptionStore {
    override fun find(code: String): DefaultDwellException? {
        val hit = (cache.get(code) as DefaultDwellException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDwellException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDwellException> = inner.list()
}

fun newCachedDefaultDwellExceptionStore(inner: DefaultDwellExceptionStore, ttl: Int, clock: Clock): DefaultDwellExceptionStore {
    return CachedDefaultDwellExceptionStore(
        inner = inner,
        cache = newTtlCache<DefaultDwellException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dwell exceptions. Empty fields match all. */
data class DefaultDwellExceptionQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDwellException): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDwellException>): MutableList<DefaultDwellException> {
        val matched = filter(items) { candidate: DefaultDwellException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDwellExceptions(store: DefaultDwellExceptionStore, query: DefaultDwellExceptionQuery): MutableList<DefaultDwellException> {
    return query.apply(store.list())
}
