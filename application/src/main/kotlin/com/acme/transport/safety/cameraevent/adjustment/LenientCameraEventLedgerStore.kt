package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps lenient camera event ledgers by code. */
interface LenientCameraEventLedgerStore {
    fun find(code: String): LenientCameraEventLedger?
    fun save(item: LenientCameraEventLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCameraEventLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCameraEventLedgerStore(
    var items: MutableMap<String, LenientCameraEventLedger>,
    var order: MutableList<String>,
) : LenientCameraEventLedgerStore {
    override fun find(code: String): LenientCameraEventLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCameraEventLedger) {
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

    override fun list(): MutableList<LenientCameraEventLedger> {
        val out = mutableListOf<LenientCameraEventLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCameraEventLedgerStore(): LenientCameraEventLedgerStore {
    return MemoryLenientCameraEventLedgerStore(
        mutableMapOf<String, LenientCameraEventLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCameraEventLedgerStore(
    var inner: LenientCameraEventLedgerStore,
    var cache: TtlCache<LenientCameraEventLedger>,
    var hits: Int,
    var misses: Int,
) : LenientCameraEventLedgerStore {
    override fun find(code: String): LenientCameraEventLedger? {
        val hit = (cache.get(code) as LenientCameraEventLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCameraEventLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCameraEventLedger> = inner.list()
}

fun newCachedLenientCameraEventLedgerStore(inner: LenientCameraEventLedgerStore, ttl: Int, clock: Clock): LenientCameraEventLedgerStore {
    return CachedLenientCameraEventLedgerStore(
        inner = inner,
        cache = newTtlCache<LenientCameraEventLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient camera event ledgers. Empty fields match all. */
data class LenientCameraEventLedgerQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCameraEventLedger): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<LenientCameraEventLedger>): MutableList<LenientCameraEventLedger> {
        val matched = filter(items) { candidate: LenientCameraEventLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCameraEventLedgers(store: LenientCameraEventLedgerStore, query: LenientCameraEventLedgerQuery): MutableList<LenientCameraEventLedger> {
    return query.apply(store.list())
}
