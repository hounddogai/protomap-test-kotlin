package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps manual violation windows by code. */
interface ManualViolationWindowStore {
    fun find(code: String): ManualViolationWindow?
    fun save(item: ManualViolationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualViolationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualViolationWindowStore(
    var items: MutableMap<String, ManualViolationWindow>,
    var order: MutableList<String>,
) : ManualViolationWindowStore {
    override fun find(code: String): ManualViolationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualViolationWindow) {
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

    override fun list(): MutableList<ManualViolationWindow> {
        val out = mutableListOf<ManualViolationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualViolationWindowStore(): ManualViolationWindowStore {
    return MemoryManualViolationWindowStore(
        mutableMapOf<String, ManualViolationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualViolationWindowStore(
    var inner: ManualViolationWindowStore,
    var cache: TtlCache<ManualViolationWindow>,
    var hits: Int,
    var misses: Int,
) : ManualViolationWindowStore {
    override fun find(code: String): ManualViolationWindow? {
        val hit = (cache.get(code) as ManualViolationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualViolationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualViolationWindow> = inner.list()
}

fun newCachedManualViolationWindowStore(inner: ManualViolationWindowStore, ttl: Int, clock: Clock): ManualViolationWindowStore {
    return CachedManualViolationWindowStore(
        inner = inner,
        cache = newTtlCache<ManualViolationWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual violation windows. Empty fields match all. */
data class ManualViolationWindowQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualViolationWindow): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualViolationWindow>): MutableList<ManualViolationWindow> {
        val matched = filter(items) { candidate: ManualViolationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualViolationWindows(store: ManualViolationWindowStore, query: ManualViolationWindowQuery): MutableList<ManualViolationWindow> {
    return query.apply(store.list())
}
