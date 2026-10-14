package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient rate card entries by code. */
interface LenientRateCardEntryStore {
    fun find(code: String): LenientRateCardEntry?
    fun save(item: LenientRateCardEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRateCardEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRateCardEntryStore(
    var items: MutableMap<String, LenientRateCardEntry>,
    var order: MutableList<String>,
) : LenientRateCardEntryStore {
    override fun find(code: String): LenientRateCardEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRateCardEntry) {
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

    override fun list(): MutableList<LenientRateCardEntry> {
        val out = mutableListOf<LenientRateCardEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRateCardEntryStore(): LenientRateCardEntryStore {
    return MemoryLenientRateCardEntryStore(
        mutableMapOf<String, LenientRateCardEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRateCardEntryStore(
    var inner: LenientRateCardEntryStore,
    var cache: TtlCache<LenientRateCardEntry>,
    var hits: Int,
    var misses: Int,
) : LenientRateCardEntryStore {
    override fun find(code: String): LenientRateCardEntry? {
        val hit = (cache.get(code) as LenientRateCardEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRateCardEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRateCardEntry> = inner.list()
}

fun newCachedLenientRateCardEntryStore(inner: LenientRateCardEntryStore, ttl: Int, clock: Clock): LenientRateCardEntryStore {
    return CachedLenientRateCardEntryStore(
        inner = inner,
        cache = newTtlCache<LenientRateCardEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient rate card entries. Empty fields match all. */
data class LenientRateCardEntryQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRateCardEntry): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientRateCardEntry>): MutableList<LenientRateCardEntry> {
        val matched = filter(items) { candidate: LenientRateCardEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRateCardEntries(store: LenientRateCardEntryStore, query: LenientRateCardEntryQuery): MutableList<LenientRateCardEntry> {
    return query.apply(store.list())
}
