package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps strict relay windows by code. */
interface StrictRelayWindowStore {
    fun find(code: String): StrictRelayWindow?
    fun save(item: StrictRelayWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRelayWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRelayWindowStore(
    var items: MutableMap<String, StrictRelayWindow>,
    var order: MutableList<String>,
) : StrictRelayWindowStore {
    override fun find(code: String): StrictRelayWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRelayWindow) {
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

    override fun list(): MutableList<StrictRelayWindow> {
        val out = mutableListOf<StrictRelayWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRelayWindowStore(): StrictRelayWindowStore {
    return MemoryStrictRelayWindowStore(mutableMapOf<String, StrictRelayWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRelayWindowStore(
    var inner: StrictRelayWindowStore,
    var cache: TtlCache<StrictRelayWindow>,
    var hits: Int,
    var misses: Int,
) : StrictRelayWindowStore {
    override fun find(code: String): StrictRelayWindow? {
        val hit = (cache.get(code) as StrictRelayWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRelayWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRelayWindow> = inner.list()
}

fun newCachedStrictRelayWindowStore(inner: StrictRelayWindowStore, ttl: Int, clock: Clock): StrictRelayWindowStore {
    return CachedStrictRelayWindowStore(
        inner = inner,
        cache = newTtlCache<StrictRelayWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict relay windows. Empty fields match all. */
data class StrictRelayWindowQuery(
    var label: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRelayWindow): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRelayWindow>): MutableList<StrictRelayWindow> {
        val matched = filter(items) { candidate: StrictRelayWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRelayWindows(store: StrictRelayWindowStore, query: StrictRelayWindowQuery): MutableList<StrictRelayWindow> {
    return query.apply(store.list())
}
