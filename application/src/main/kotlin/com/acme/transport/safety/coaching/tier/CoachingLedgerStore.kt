package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching ledgers by code. */
interface CoachingLedgerStore {
    fun find(code: String): CoachingLedger?
    fun save(item: CoachingLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingLedgerStore(
    var items: MutableMap<String, CoachingLedger>,
    var order: MutableList<String>,
) : CoachingLedgerStore {
    override fun find(code: String): CoachingLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingLedger) {
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

    override fun list(): MutableList<CoachingLedger> {
        val out = mutableListOf<CoachingLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingLedgerStore(): CoachingLedgerStore {
    return MemoryCoachingLedgerStore(mutableMapOf<String, CoachingLedger>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingLedgerStore(
    var inner: CoachingLedgerStore,
    var cache: TtlCache<CoachingLedger>,
    var hits: Int,
    var misses: Int,
) : CoachingLedgerStore {
    override fun find(code: String): CoachingLedger? {
        val hit = (cache.get(code) as CoachingLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingLedger> = inner.list()
}

fun newCachedCoachingLedgerStore(inner: CoachingLedgerStore, ttl: Int, clock: Clock): CoachingLedgerStore {
    return CachedCoachingLedgerStore(
        inner = inner,
        cache = newTtlCache<CoachingLedger>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching ledgers. Empty fields match all. */
data class CoachingLedgerQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingLedger>): MutableList<CoachingLedger> {
        val matched = filter(items) { candidate: CoachingLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingLedgers(store: CoachingLedgerStore, query: CoachingLedgerQuery): MutableList<CoachingLedger> {
    return query.apply(store.list())
}
