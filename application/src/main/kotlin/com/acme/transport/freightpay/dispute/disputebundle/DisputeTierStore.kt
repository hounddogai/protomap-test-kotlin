package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute tiers by code. */
interface DisputeTierStore {
    fun find(code: String): DisputeTier?
    fun save(item: DisputeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeTierStore(
    var items: MutableMap<String, DisputeTier>,
    var order: MutableList<String>,
) : DisputeTierStore {
    override fun find(code: String): DisputeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeTier) {
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

    override fun list(): MutableList<DisputeTier> {
        val out = mutableListOf<DisputeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeTierStore(): DisputeTierStore {
    return MemoryDisputeTierStore(mutableMapOf<String, DisputeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeTierStore(
    var inner: DisputeTierStore,
    var cache: TtlCache<DisputeTier>,
    var hits: Int,
    var misses: Int,
) : DisputeTierStore {
    override fun find(code: String): DisputeTier? {
        val hit = (cache.get(code) as DisputeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeTier> = inner.list()
}

fun newCachedDisputeTierStore(inner: DisputeTierStore, ttl: Int, clock: Clock): DisputeTierStore {
    return CachedDisputeTierStore(
        inner = inner,
        cache = newTtlCache<DisputeTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute tiers. Empty fields match all. */
data class DisputeTierQuery(
    var channel: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeTier): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeTier>): MutableList<DisputeTier> {
        val matched = filter(items) { candidate: DisputeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeTiers(store: DisputeTierStore, query: DisputeTierQuery): MutableList<DisputeTier> {
    return query.apply(store.list())
}
