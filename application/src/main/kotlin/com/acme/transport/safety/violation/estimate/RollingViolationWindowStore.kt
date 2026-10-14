package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling violation windows by code. */
interface RollingViolationWindowStore {
    fun find(code: String): RollingViolationWindow?
    fun save(item: RollingViolationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingViolationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingViolationWindowStore(
    var items: MutableMap<String, RollingViolationWindow>,
    var order: MutableList<String>,
) : RollingViolationWindowStore {
    override fun find(code: String): RollingViolationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingViolationWindow) {
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

    override fun list(): MutableList<RollingViolationWindow> {
        val out = mutableListOf<RollingViolationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingViolationWindowStore(): RollingViolationWindowStore {
    return MemoryRollingViolationWindowStore(
        mutableMapOf<String, RollingViolationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingViolationWindowStore(
    var inner: RollingViolationWindowStore,
    var cache: TtlCache<RollingViolationWindow>,
    var hits: Int,
    var misses: Int,
) : RollingViolationWindowStore {
    override fun find(code: String): RollingViolationWindow? {
        val hit = (cache.get(code) as RollingViolationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingViolationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingViolationWindow> = inner.list()
}

fun newCachedRollingViolationWindowStore(inner: RollingViolationWindowStore, ttl: Int, clock: Clock): RollingViolationWindowStore {
    return CachedRollingViolationWindowStore(
        inner = inner,
        cache = newTtlCache<RollingViolationWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling violation windows. Empty fields match all. */
data class RollingViolationWindowQuery(
    var region: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingViolationWindow): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingViolationWindow>): MutableList<RollingViolationWindow> {
        val matched = filter(items) { candidate: RollingViolationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingViolationWindows(store: RollingViolationWindowStore, query: RollingViolationWindowQuery): MutableList<RollingViolationWindow> {
    return query.apply(store.list())
}
