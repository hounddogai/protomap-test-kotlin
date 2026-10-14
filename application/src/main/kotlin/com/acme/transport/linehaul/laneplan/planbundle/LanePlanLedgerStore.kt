package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan ledgers by code. */
interface LanePlanLedgerStore {
    fun find(code: String): LanePlanLedger?
    fun save(item: LanePlanLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanLedgerStore(
    var items: MutableMap<String, LanePlanLedger>,
    var order: MutableList<String>,
) : LanePlanLedgerStore {
    override fun find(code: String): LanePlanLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanLedger) {
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

    override fun list(): MutableList<LanePlanLedger> {
        val out = mutableListOf<LanePlanLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanLedgerStore(): LanePlanLedgerStore {
    return MemoryLanePlanLedgerStore(mutableMapOf<String, LanePlanLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanLedgerStore(
    var inner: LanePlanLedgerStore,
    var cache: TtlCache<LanePlanLedger>,
    var hits: Int,
    var misses: Int,
) : LanePlanLedgerStore {
    override fun find(code: String): LanePlanLedger? {
        val hit = (cache.get(code) as LanePlanLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanLedger> = inner.list()
}

fun newCachedLanePlanLedgerStore(inner: LanePlanLedgerStore, ttl: Int, clock: Clock): LanePlanLedgerStore {
    return CachedLanePlanLedgerStore(
        inner = inner,
        cache = newTtlCache<LanePlanLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan ledgers. Empty fields match all. */
data class LanePlanLedgerQuery(
    var source: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanLedger): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanLedger>): MutableList<LanePlanLedger> {
        val matched = filter(items) { candidate: LanePlanLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanLedgers(store: LanePlanLedgerStore, query: LanePlanLedgerQuery): MutableList<LanePlanLedger> {
    return query.apply(store.list())
}
