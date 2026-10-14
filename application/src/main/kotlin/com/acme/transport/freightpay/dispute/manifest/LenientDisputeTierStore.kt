package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient dispute tiers by code. */
interface LenientDisputeTierStore {
    fun find(code: String): LenientDisputeTier?
    fun save(item: LenientDisputeTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDisputeTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDisputeTierStore(
    var items: MutableMap<String, LenientDisputeTier>,
    var order: MutableList<String>,
) : LenientDisputeTierStore {
    override fun find(code: String): LenientDisputeTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDisputeTier) {
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

    override fun list(): MutableList<LenientDisputeTier> {
        val out = mutableListOf<LenientDisputeTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDisputeTierStore(): LenientDisputeTierStore {
    return MemoryLenientDisputeTierStore(mutableMapOf<String, LenientDisputeTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDisputeTierStore(
    var inner: LenientDisputeTierStore,
    var cache: TtlCache<LenientDisputeTier>,
    var hits: Int,
    var misses: Int,
) : LenientDisputeTierStore {
    override fun find(code: String): LenientDisputeTier? {
        val hit = (cache.get(code) as LenientDisputeTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDisputeTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDisputeTier> = inner.list()
}

fun newCachedLenientDisputeTierStore(inner: LenientDisputeTierStore, ttl: Int, clock: Clock): LenientDisputeTierStore {
    return CachedLenientDisputeTierStore(
        inner = inner,
        cache = newTtlCache<LenientDisputeTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dispute tiers. Empty fields match all. */
data class LenientDisputeTierQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDisputeTier): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDisputeTier>): MutableList<LenientDisputeTier> {
        val matched = filter(items) { candidate: LenientDisputeTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDisputeTiers(store: LenientDisputeTierStore, query: LenientDisputeTierQuery): MutableList<LenientDisputeTier> {
    return query.apply(store.list())
}
