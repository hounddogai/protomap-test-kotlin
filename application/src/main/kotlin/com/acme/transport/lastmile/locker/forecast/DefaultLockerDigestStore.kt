package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default locker digests by code. */
interface DefaultLockerDigestStore {
    fun find(code: String): DefaultLockerDigest?
    fun save(item: DefaultLockerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultLockerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultLockerDigestStore(
    var items: MutableMap<String, DefaultLockerDigest>,
    var order: MutableList<String>,
) : DefaultLockerDigestStore {
    override fun find(code: String): DefaultLockerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultLockerDigest) {
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

    override fun list(): MutableList<DefaultLockerDigest> {
        val out = mutableListOf<DefaultLockerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultLockerDigestStore(): DefaultLockerDigestStore {
    return MemoryDefaultLockerDigestStore(mutableMapOf<String, DefaultLockerDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultLockerDigestStore(
    var inner: DefaultLockerDigestStore,
    var cache: TtlCache<DefaultLockerDigest>,
    var hits: Int,
    var misses: Int,
) : DefaultLockerDigestStore {
    override fun find(code: String): DefaultLockerDigest? {
        val hit = (cache.get(code) as DefaultLockerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultLockerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultLockerDigest> = inner.list()
}

fun newCachedDefaultLockerDigestStore(inner: DefaultLockerDigestStore, ttl: Int, clock: Clock): DefaultLockerDigestStore {
    return CachedDefaultLockerDigestStore(
        inner = inner,
        cache = newTtlCache<DefaultLockerDigest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default locker digests. Empty fields match all. */
data class DefaultLockerDigestQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultLockerDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultLockerDigest>): MutableList<DefaultLockerDigest> {
        val matched = filter(items) { candidate: DefaultLockerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultLockerDigests(store: DefaultLockerDigestStore, query: DefaultLockerDigestQuery): MutableList<DefaultLockerDigest> {
    return query.apply(store.list())
}
