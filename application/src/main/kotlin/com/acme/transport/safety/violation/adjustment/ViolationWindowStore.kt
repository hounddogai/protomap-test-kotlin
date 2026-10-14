package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation windows by code. */
interface ViolationWindowStore {
    fun find(code: String): ViolationWindow?
    fun save(item: ViolationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationWindowStore(
    var items: MutableMap<String, ViolationWindow>,
    var order: MutableList<String>,
) : ViolationWindowStore {
    override fun find(code: String): ViolationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationWindow) {
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

    override fun list(): MutableList<ViolationWindow> {
        val out = mutableListOf<ViolationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationWindowStore(): ViolationWindowStore {
    return MemoryViolationWindowStore(mutableMapOf<String, ViolationWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationWindowStore(
    var inner: ViolationWindowStore,
    var cache: TtlCache<ViolationWindow>,
    var hits: Int,
    var misses: Int,
) : ViolationWindowStore {
    override fun find(code: String): ViolationWindow? {
        val hit = (cache.get(code) as ViolationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationWindow> = inner.list()
}

fun newCachedViolationWindowStore(inner: ViolationWindowStore, ttl: Int, clock: Clock): ViolationWindowStore {
    return CachedViolationWindowStore(
        inner = inner,
        cache = newTtlCache<ViolationWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation windows. Empty fields match all. */
data class ViolationWindowQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationWindow>): MutableList<ViolationWindow> {
        val matched = filter(items) { candidate: ViolationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationWindows(store: ViolationWindowStore, query: ViolationWindowQuery): MutableList<ViolationWindow> {
    return query.apply(store.list())
}
