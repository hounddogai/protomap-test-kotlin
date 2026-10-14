package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default parcel exceptions by code. */
interface DefaultParcelExceptionStore {
    fun find(code: String): DefaultParcelException?
    fun save(item: DefaultParcelException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultParcelException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultParcelExceptionStore(
    var items: MutableMap<String, DefaultParcelException>,
    var order: MutableList<String>,
) : DefaultParcelExceptionStore {
    override fun find(code: String): DefaultParcelException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultParcelException) {
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

    override fun list(): MutableList<DefaultParcelException> {
        val out = mutableListOf<DefaultParcelException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultParcelExceptionStore(): DefaultParcelExceptionStore {
    return MemoryDefaultParcelExceptionStore(
        mutableMapOf<String, DefaultParcelException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultParcelExceptionStore(
    var inner: DefaultParcelExceptionStore,
    var cache: TtlCache<DefaultParcelException>,
    var hits: Int,
    var misses: Int,
) : DefaultParcelExceptionStore {
    override fun find(code: String): DefaultParcelException? {
        val hit = (cache.get(code) as DefaultParcelException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultParcelException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultParcelException> = inner.list()
}

fun newCachedDefaultParcelExceptionStore(inner: DefaultParcelExceptionStore, ttl: Int, clock: Clock): DefaultParcelExceptionStore {
    return CachedDefaultParcelExceptionStore(
        inner = inner,
        cache = newTtlCache<DefaultParcelException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default parcel exceptions. Empty fields match all. */
data class DefaultParcelExceptionQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultParcelException): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultParcelException>): MutableList<DefaultParcelException> {
        val matched = filter(items) { candidate: DefaultParcelException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultParcelExceptions(store: DefaultParcelExceptionStore, query: DefaultParcelExceptionQuery): MutableList<DefaultParcelException> {
    return query.apply(store.list())
}
