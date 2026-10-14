package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul ledgers by code. */
interface BackhaulLedgerStore {
    fun find(code: String): BackhaulLedger?
    fun save(item: BackhaulLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulLedgerStore(
    var items: MutableMap<String, BackhaulLedger>,
    var order: MutableList<String>,
) : BackhaulLedgerStore {
    override fun find(code: String): BackhaulLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulLedger) {
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

    override fun list(): MutableList<BackhaulLedger> {
        val out = mutableListOf<BackhaulLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulLedgerStore(): BackhaulLedgerStore {
    return MemoryBackhaulLedgerStore(mutableMapOf<String, BackhaulLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulLedgerStore(
    var inner: BackhaulLedgerStore,
    var cache: TtlCache<BackhaulLedger>,
    var hits: Int,
    var misses: Int,
) : BackhaulLedgerStore {
    override fun find(code: String): BackhaulLedger? {
        val hit = (cache.get(code) as BackhaulLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulLedger> = inner.list()
}

fun newCachedBackhaulLedgerStore(inner: BackhaulLedgerStore, ttl: Int, clock: Clock): BackhaulLedgerStore {
    return CachedBackhaulLedgerStore(
        inner = inner,
        cache = newTtlCache<BackhaulLedger>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul ledgers. Empty fields match all. */
data class BackhaulLedgerQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulLedger): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BackhaulLedger>): MutableList<BackhaulLedger> {
        val matched = filter(items) { candidate: BackhaulLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulLedgers(store: BackhaulLedgerStore, query: BackhaulLedgerQuery): MutableList<BackhaulLedger> {
    return query.apply(store.list())
}
