package com.acme.transport.network.zone.zonetier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone ledgers by code. */
interface ZoneLedgerStore {
    fun find(code: String): ZoneLedger?
    fun save(item: ZoneLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneLedgerStore(
    var items: MutableMap<String, ZoneLedger>,
    var order: MutableList<String>,
) : ZoneLedgerStore {
    override fun find(code: String): ZoneLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneLedger) {
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

    override fun list(): MutableList<ZoneLedger> {
        val out = mutableListOf<ZoneLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneLedgerStore(): ZoneLedgerStore {
    return MemoryZoneLedgerStore(mutableMapOf<String, ZoneLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneLedgerStore(
    var inner: ZoneLedgerStore,
    var cache: TtlCache<ZoneLedger>,
    var hits: Int,
    var misses: Int,
) : ZoneLedgerStore {
    override fun find(code: String): ZoneLedger? {
        val hit = (cache.get(code) as ZoneLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneLedger> = inner.list()
}

fun newCachedZoneLedgerStore(inner: ZoneLedgerStore, ttl: Int, clock: Clock): ZoneLedgerStore {
    return CachedZoneLedgerStore(
        inner = inner,
        cache = newTtlCache<ZoneLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone ledgers. Empty fields match all. */
data class ZoneLedgerQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneLedger>): MutableList<ZoneLedger> {
        val matched = filter(items) { candidate: ZoneLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneLedgers(store: ZoneLedgerStore, query: ZoneLedgerQuery): MutableList<ZoneLedger> {
    return query.apply(store.list())
}
