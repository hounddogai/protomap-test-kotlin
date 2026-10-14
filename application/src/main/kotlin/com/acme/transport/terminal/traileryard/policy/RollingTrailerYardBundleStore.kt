package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard bundles by code. */
interface RollingTrailerYardBundleStore {
    fun find(code: String): RollingTrailerYardBundle?
    fun save(item: RollingTrailerYardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardBundleStore(
    var items: MutableMap<String, RollingTrailerYardBundle>,
    var order: MutableList<String>,
) : RollingTrailerYardBundleStore {
    override fun find(code: String): RollingTrailerYardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardBundle) {
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

    override fun list(): MutableList<RollingTrailerYardBundle> {
        val out = mutableListOf<RollingTrailerYardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardBundleStore(): RollingTrailerYardBundleStore {
    return MemoryRollingTrailerYardBundleStore(
        mutableMapOf<String, RollingTrailerYardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardBundleStore(
    var inner: RollingTrailerYardBundleStore,
    var cache: TtlCache<RollingTrailerYardBundle>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardBundleStore {
    override fun find(code: String): RollingTrailerYardBundle? {
        val hit = (cache.get(code) as RollingTrailerYardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardBundle> = inner.list()
}

fun newCachedRollingTrailerYardBundleStore(inner: RollingTrailerYardBundleStore, ttl: Int, clock: Clock): RollingTrailerYardBundleStore {
    return CachedRollingTrailerYardBundleStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard bundles. Empty fields match all. */
data class RollingTrailerYardBundleQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardBundle): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingTrailerYardBundle>): MutableList<RollingTrailerYardBundle> {
        val matched = filter(items) { candidate: RollingTrailerYardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardBundles(store: RollingTrailerYardBundleStore, query: RollingTrailerYardBundleQuery): MutableList<RollingTrailerYardBundle> {
    return query.apply(store.list())
}
