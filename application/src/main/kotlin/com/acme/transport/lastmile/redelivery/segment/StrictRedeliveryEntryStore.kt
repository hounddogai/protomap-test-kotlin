package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict redelivery entries by code. */
interface StrictRedeliveryEntryStore {
    fun find(code: String): StrictRedeliveryEntry?
    fun save(item: StrictRedeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRedeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRedeliveryEntryStore(
    var items: MutableMap<String, StrictRedeliveryEntry>,
    var order: MutableList<String>,
) : StrictRedeliveryEntryStore {
    override fun find(code: String): StrictRedeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRedeliveryEntry) {
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

    override fun list(): MutableList<StrictRedeliveryEntry> {
        val out = mutableListOf<StrictRedeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRedeliveryEntryStore(): StrictRedeliveryEntryStore {
    return MemoryStrictRedeliveryEntryStore(
        mutableMapOf<String, StrictRedeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRedeliveryEntryStore(
    var inner: StrictRedeliveryEntryStore,
    var cache: TtlCache<StrictRedeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : StrictRedeliveryEntryStore {
    override fun find(code: String): StrictRedeliveryEntry? {
        val hit = (cache.get(code) as StrictRedeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRedeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRedeliveryEntry> = inner.list()
}

fun newCachedStrictRedeliveryEntryStore(inner: StrictRedeliveryEntryStore, ttl: Int, clock: Clock): StrictRedeliveryEntryStore {
    return CachedStrictRedeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<StrictRedeliveryEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict redelivery entries. Empty fields match all. */
data class StrictRedeliveryEntryQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRedeliveryEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRedeliveryEntry>): MutableList<StrictRedeliveryEntry> {
        val matched = filter(items) { candidate: StrictRedeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRedeliveryEntries(store: StrictRedeliveryEntryStore, query: StrictRedeliveryEntryQuery): MutableList<StrictRedeliveryEntry> {
    return query.apply(store.list())
}
