package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling relay entries by code. */
interface RollingRelayEntryStore {
    fun find(code: String): RollingRelayEntry?
    fun save(item: RollingRelayEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingRelayEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingRelayEntryStore(
    var items: MutableMap<String, RollingRelayEntry>,
    var order: MutableList<String>,
) : RollingRelayEntryStore {
    override fun find(code: String): RollingRelayEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingRelayEntry) {
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

    override fun list(): MutableList<RollingRelayEntry> {
        val out = mutableListOf<RollingRelayEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingRelayEntryStore(): RollingRelayEntryStore {
    return MemoryRollingRelayEntryStore(mutableMapOf<String, RollingRelayEntry>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingRelayEntryStore(
    var inner: RollingRelayEntryStore,
    var cache: TtlCache<RollingRelayEntry>,
    var hits: Int,
    var misses: Int,
) : RollingRelayEntryStore {
    override fun find(code: String): RollingRelayEntry? {
        val hit = (cache.get(code) as RollingRelayEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingRelayEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingRelayEntry> = inner.list()
}

fun newCachedRollingRelayEntryStore(inner: RollingRelayEntryStore, ttl: Int, clock: Clock): RollingRelayEntryStore {
    return CachedRollingRelayEntryStore(
        inner = inner,
        cache = newTtlCache<RollingRelayEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling relay entries. Empty fields match all. */
data class RollingRelayEntryQuery(
    var source: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingRelayEntry): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingRelayEntry>): MutableList<RollingRelayEntry> {
        val matched = filter(items) { candidate: RollingRelayEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingRelayEntries(store: RollingRelayEntryStore, query: RollingRelayEntryQuery): MutableList<RollingRelayEntry> {
    return query.apply(store.list())
}
