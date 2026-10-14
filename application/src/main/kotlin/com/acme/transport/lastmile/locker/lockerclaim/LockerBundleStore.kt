package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps locker bundles by code. */
interface LockerBundleStore {
    fun find(code: String): LockerBundle?
    fun save(item: LockerBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LockerBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLockerBundleStore(
    var items: MutableMap<String, LockerBundle>,
    var order: MutableList<String>,
) : LockerBundleStore {
    override fun find(code: String): LockerBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LockerBundle) {
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

    override fun list(): MutableList<LockerBundle> {
        val out = mutableListOf<LockerBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLockerBundleStore(): LockerBundleStore {
    return MemoryLockerBundleStore(mutableMapOf<String, LockerBundle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLockerBundleStore(
    var inner: LockerBundleStore,
    var cache: TtlCache<LockerBundle>,
    var hits: Int,
    var misses: Int,
) : LockerBundleStore {
    override fun find(code: String): LockerBundle? {
        val hit = (cache.get(code) as LockerBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LockerBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LockerBundle> = inner.list()
}

fun newCachedLockerBundleStore(inner: LockerBundleStore, ttl: Int, clock: Clock): LockerBundleStore {
    return CachedLockerBundleStore(
        inner = inner,
        cache = newTtlCache<LockerBundle>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects locker bundles. Empty fields match all. */
data class LockerBundleQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LockerBundle): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LockerBundle>): MutableList<LockerBundle> {
        val matched = filter(items) { candidate: LockerBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLockerBundles(store: LockerBundleStore, query: LockerBundleQuery): MutableList<LockerBundle> {
    return query.apply(store.list())
}
