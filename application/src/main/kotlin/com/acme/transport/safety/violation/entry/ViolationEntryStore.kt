package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation entries by code. */
interface ViolationEntryStore {
    fun find(code: String): ViolationEntry?
    fun save(item: ViolationEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationEntryStore(
    var items: MutableMap<String, ViolationEntry>,
    var order: MutableList<String>,
) : ViolationEntryStore {
    override fun find(code: String): ViolationEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationEntry) {
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

    override fun list(): MutableList<ViolationEntry> {
        val out = mutableListOf<ViolationEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationEntryStore(): ViolationEntryStore {
    return MemoryViolationEntryStore(mutableMapOf<String, ViolationEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationEntryStore(
    var inner: ViolationEntryStore,
    var cache: TtlCache<ViolationEntry>,
    var hits: Int,
    var misses: Int,
) : ViolationEntryStore {
    override fun find(code: String): ViolationEntry? {
        val hit = (cache.get(code) as ViolationEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationEntry> = inner.list()
}

fun newCachedViolationEntryStore(inner: ViolationEntryStore, ttl: Int, clock: Clock): ViolationEntryStore {
    return CachedViolationEntryStore(
        inner = inner,
        cache = newTtlCache<ViolationEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation entries. Empty fields match all. */
data class ViolationEntryQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationEntry): Boolean {
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

    fun apply(items: MutableList<ViolationEntry>): MutableList<ViolationEntry> {
        val matched = filter(items) { candidate: ViolationEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationEntries(store: ViolationEntryStore, query: ViolationEntryQuery): MutableList<ViolationEntry> {
    return query.apply(store.list())
}
