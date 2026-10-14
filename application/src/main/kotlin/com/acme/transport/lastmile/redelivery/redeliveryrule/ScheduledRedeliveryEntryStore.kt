package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled redelivery entries by code. */
interface ScheduledRedeliveryEntryStore {
    fun find(code: String): ScheduledRedeliveryEntry?
    fun save(item: ScheduledRedeliveryEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRedeliveryEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRedeliveryEntryStore(
    var items: MutableMap<String, ScheduledRedeliveryEntry>,
    var order: MutableList<String>,
) : ScheduledRedeliveryEntryStore {
    override fun find(code: String): ScheduledRedeliveryEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRedeliveryEntry) {
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

    override fun list(): MutableList<ScheduledRedeliveryEntry> {
        val out = mutableListOf<ScheduledRedeliveryEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRedeliveryEntryStore(): ScheduledRedeliveryEntryStore {
    return MemoryScheduledRedeliveryEntryStore(
        mutableMapOf<String, ScheduledRedeliveryEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRedeliveryEntryStore(
    var inner: ScheduledRedeliveryEntryStore,
    var cache: TtlCache<ScheduledRedeliveryEntry>,
    var hits: Int,
    var misses: Int,
) : ScheduledRedeliveryEntryStore {
    override fun find(code: String): ScheduledRedeliveryEntry? {
        val hit = (cache.get(code) as ScheduledRedeliveryEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRedeliveryEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRedeliveryEntry> = inner.list()
}

fun newCachedScheduledRedeliveryEntryStore(inner: ScheduledRedeliveryEntryStore, ttl: Int, clock: Clock): ScheduledRedeliveryEntryStore {
    return CachedScheduledRedeliveryEntryStore(
        inner = inner,
        cache = newTtlCache<ScheduledRedeliveryEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled redelivery entries. Empty fields match all. */
data class ScheduledRedeliveryEntryQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRedeliveryEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRedeliveryEntry>): MutableList<ScheduledRedeliveryEntry> {
        val matched = filter(items) { candidate: ScheduledRedeliveryEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRedeliveryEntries(store: ScheduledRedeliveryEntryStore, query: ScheduledRedeliveryEntryQuery): MutableList<ScheduledRedeliveryEntry> {
    return query.apply(store.list())
}
