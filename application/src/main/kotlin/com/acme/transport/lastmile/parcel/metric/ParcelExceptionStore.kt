package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel exceptions by code. */
interface ParcelExceptionStore {
    fun find(code: String): ParcelException?
    fun save(item: ParcelException)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelExceptionStore(
    var items: MutableMap<String, ParcelException>,
    var order: MutableList<String>,
) : ParcelExceptionStore {
    override fun find(code: String): ParcelException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelException) {
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

    override fun list(): MutableList<ParcelException> {
        val out = mutableListOf<ParcelException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelExceptionStore(): ParcelExceptionStore {
    return MemoryParcelExceptionStore(mutableMapOf<String, ParcelException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelExceptionStore(
    var inner: ParcelExceptionStore,
    var cache: TtlCache<ParcelException>,
    var hits: Int,
    var misses: Int,
) : ParcelExceptionStore {
    override fun find(code: String): ParcelException? {
        val hit = (cache.get(code) as ParcelException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelException> = inner.list()
}

fun newCachedParcelExceptionStore(inner: ParcelExceptionStore, ttl: Int, clock: Clock): ParcelExceptionStore {
    return CachedParcelExceptionStore(
        inner = inner,
        cache = newTtlCache<ParcelException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel exceptions. Empty fields match all. */
data class ParcelExceptionQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelException): Boolean {
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

    fun apply(items: MutableList<ParcelException>): MutableList<ParcelException> {
        val matched = filter(items) { candidate: ParcelException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelExceptions(store: ParcelExceptionStore, query: ParcelExceptionQuery): MutableList<ParcelException> {
    return query.apply(store.list())
}
