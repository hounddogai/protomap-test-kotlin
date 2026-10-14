package com.acme.transport.lastmile.locker.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker entries by code. */
interface LockerEntryStore {
    fun find(code: String): LockerEntry?
    fun save(item: LockerEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerEntryStore(
    var items: MutableMap<String, LockerEntry>,
    var order: MutableList<String>,
) : LockerEntryStore {
    override fun find(code: String): LockerEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerEntry) {
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

    override fun list(): MutableList<LockerEntry> {
        val out = mutableListOf<LockerEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerEntryStore(): LockerEntryStore {
    return MemoryLockerEntryStore(mutableMapOf<String, LockerEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerEntryStore(
    var inner: LockerEntryStore,
    var cache: TtlCache<LockerEntry>,
    var hits: Int,
    var misses: Int,
) : LockerEntryStore {
    override fun find(code: String): LockerEntry? {
        val hit = (cache.get(code) as LockerEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerEntry> = inner.list()
}

fun newCachedLockerEntryStore(inner: LockerEntryStore, ttl: Int, clock: Clock): LockerEntryStore {
    return CachedLockerEntryStore(
        inner = inner,
        cache = newTtlCache<LockerEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker entries. Empty fields match all. */
data class LockerEntryQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LockerEntry>): MutableList<LockerEntry> {
        val matched = filter(items) { candidate: LockerEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerEntries(store: LockerEntryStore, query: LockerEntryQuery): MutableList<LockerEntry> {
    return query.apply(store.list())
}
