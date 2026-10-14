package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rest break ledgers by code. */
interface ScheduledRestBreakLedgerStore {
    fun find(code: String): ScheduledRestBreakLedger?
    fun save(item: ScheduledRestBreakLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRestBreakLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRestBreakLedgerStore(
    var items: MutableMap<String, ScheduledRestBreakLedger>,
    var order: MutableList<String>,
) : ScheduledRestBreakLedgerStore {
    override fun find(code: String): ScheduledRestBreakLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRestBreakLedger) {
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

    override fun list(): MutableList<ScheduledRestBreakLedger> {
        val out = mutableListOf<ScheduledRestBreakLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRestBreakLedgerStore(): ScheduledRestBreakLedgerStore {
    return MemoryScheduledRestBreakLedgerStore(
        mutableMapOf<String, ScheduledRestBreakLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRestBreakLedgerStore(
    var inner: ScheduledRestBreakLedgerStore,
    var cache: TtlCache<ScheduledRestBreakLedger>,
    var hits: Int,
    var misses: Int,
) : ScheduledRestBreakLedgerStore {
    override fun find(code: String): ScheduledRestBreakLedger? {
        val hit = (cache.get(code) as ScheduledRestBreakLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRestBreakLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRestBreakLedger> = inner.list()
}

fun newCachedScheduledRestBreakLedgerStore(inner: ScheduledRestBreakLedgerStore, ttl: Int, clock: Clock): ScheduledRestBreakLedgerStore {
    return CachedScheduledRestBreakLedgerStore(
        inner = inner,
        cache = newTtlCache<ScheduledRestBreakLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rest break ledgers. Empty fields match all. */
data class ScheduledRestBreakLedgerQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRestBreakLedger): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRestBreakLedger>): MutableList<ScheduledRestBreakLedger> {
        val matched = filter(items) { candidate: ScheduledRestBreakLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRestBreakLedgers(store: ScheduledRestBreakLedgerStore, query: ScheduledRestBreakLedgerQuery): MutableList<ScheduledRestBreakLedger> {
    return query.apply(store.list())
}
