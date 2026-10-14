package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery entries by code. */
interface RedeliveryEntryStore {
    fun find(code: String): RedeliveryEntry?
    fun save(item: RedeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryEntryStore(
    var items: MutableMap<String, RedeliveryEntry>,
    var order: MutableList<String>,
) : RedeliveryEntryStore {
    override fun find(code: String): RedeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryEntry) {
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

    override fun list(): MutableList<RedeliveryEntry> {
        val out = mutableListOf<RedeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryEntryStore(): RedeliveryEntryStore {
    return MemoryRedeliveryEntryStore(mutableMapOf<String, RedeliveryEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryEntryStore(
    var inner: RedeliveryEntryStore,
    var cache: TtlCache<RedeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : RedeliveryEntryStore {
    override fun find(code: String): RedeliveryEntry? {
        val hit = (cache.get(code) as RedeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryEntry> = inner.list()
}

fun newCachedRedeliveryEntryStore(inner: RedeliveryEntryStore, ttl: Int, clock: Clock): RedeliveryEntryStore {
    return CachedRedeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<RedeliveryEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery entries. Empty fields match all. */
data class RedeliveryEntryQuery(
    var locale: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryEntry): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryEntry>): MutableList<RedeliveryEntry> {
        val matched = filter(items) { candidate: RedeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryEntries(store: RedeliveryEntryStore, query: RedeliveryEntryQuery): MutableList<RedeliveryEntry> {
    return query.apply(store.list())
}
