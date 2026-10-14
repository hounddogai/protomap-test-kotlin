package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default ev charge claims by code. */
interface DefaultEvChargeClaimStore {
    fun find(code: String): DefaultEvChargeClaim?
    fun save(item: DefaultEvChargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultEvChargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultEvChargeClaimStore(
    var items: MutableMap<String, DefaultEvChargeClaim>,
    var order: MutableList<String>,
) : DefaultEvChargeClaimStore {
    override fun find(code: String): DefaultEvChargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultEvChargeClaim) {
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

    override fun list(): MutableList<DefaultEvChargeClaim> {
        val out = mutableListOf<DefaultEvChargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultEvChargeClaimStore(): DefaultEvChargeClaimStore {
    return MemoryDefaultEvChargeClaimStore(
        mutableMapOf<String, DefaultEvChargeClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultEvChargeClaimStore(
    var inner: DefaultEvChargeClaimStore,
    var cache: TtlCache<DefaultEvChargeClaim>,
    var hits: Int,
    var misses: Int,
) : DefaultEvChargeClaimStore {
    override fun find(code: String): DefaultEvChargeClaim? {
        val hit = (cache.get(code) as DefaultEvChargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultEvChargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultEvChargeClaim> = inner.list()
}

fun newCachedDefaultEvChargeClaimStore(inner: DefaultEvChargeClaimStore, ttl: Int, clock: Clock): DefaultEvChargeClaimStore {
    return CachedDefaultEvChargeClaimStore(
        inner = inner,
        cache = newTtlCache<DefaultEvChargeClaim>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default ev charge claims. Empty fields match all. */
data class DefaultEvChargeClaimQuery(
    var channel: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultEvChargeClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultEvChargeClaim>): MutableList<DefaultEvChargeClaim> {
        val matched = filter(items) { candidate: DefaultEvChargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultEvChargeClaims(store: DefaultEvChargeClaimStore, query: DefaultEvChargeClaimQuery): MutableList<DefaultEvChargeClaim> {
    return query.apply(store.list())
}
