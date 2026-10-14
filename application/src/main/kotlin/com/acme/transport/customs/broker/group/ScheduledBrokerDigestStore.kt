package com.acme.transport.customs.broker.group

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled broker digests by code. */
interface ScheduledBrokerDigestStore {
    fun find(code: String): ScheduledBrokerDigest?
    fun save(item: ScheduledBrokerDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledBrokerDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledBrokerDigestStore(
    var items: MutableMap<String, ScheduledBrokerDigest>,
    var order: MutableList<String>,
) : ScheduledBrokerDigestStore {
    override fun find(code: String): ScheduledBrokerDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledBrokerDigest) {
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

    override fun list(): MutableList<ScheduledBrokerDigest> {
        val out = mutableListOf<ScheduledBrokerDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledBrokerDigestStore(): ScheduledBrokerDigestStore {
    return MemoryScheduledBrokerDigestStore(
        mutableMapOf<String, ScheduledBrokerDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledBrokerDigestStore(
    var inner: ScheduledBrokerDigestStore,
    var cache: TtlCache<ScheduledBrokerDigest>,
    var hits: Int,
    var misses: Int,
) : ScheduledBrokerDigestStore {
    override fun find(code: String): ScheduledBrokerDigest? {
        val hit = (cache.get(code) as ScheduledBrokerDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledBrokerDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledBrokerDigest> = inner.list()
}

fun newCachedScheduledBrokerDigestStore(inner: ScheduledBrokerDigestStore, ttl: Int, clock: Clock): ScheduledBrokerDigestStore {
    return CachedScheduledBrokerDigestStore(
        inner = inner,
        cache = newTtlCache<ScheduledBrokerDigest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled broker digests. Empty fields match all. */
data class ScheduledBrokerDigestQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledBrokerDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledBrokerDigest>): MutableList<ScheduledBrokerDigest> {
        val matched = filter(items) { candidate: ScheduledBrokerDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledBrokerDigests(store: ScheduledBrokerDigestStore, query: ScheduledBrokerDigestQuery): MutableList<ScheduledBrokerDigest> {
    return query.apply(store.list())
}
