package com.acme.transport.terminal.traileryard.yardcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy trailer yard bundles by code. */
interface LegacyTrailerYardBundleStore {
    fun find(code: String): LegacyTrailerYardBundle?
    fun save(item: LegacyTrailerYardBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerYardBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerYardBundleStore(
    var items: MutableMap<String, LegacyTrailerYardBundle>,
    var order: MutableList<String>,
) : LegacyTrailerYardBundleStore {
    override fun find(code: String): LegacyTrailerYardBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerYardBundle) {
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

    override fun list(): MutableList<LegacyTrailerYardBundle> {
        val out = mutableListOf<LegacyTrailerYardBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerYardBundleStore(): LegacyTrailerYardBundleStore {
    return MemoryLegacyTrailerYardBundleStore(
        mutableMapOf<String, LegacyTrailerYardBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerYardBundleStore(
    var inner: LegacyTrailerYardBundleStore,
    var cache: TtlCache<LegacyTrailerYardBundle>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerYardBundleStore {
    override fun find(code: String): LegacyTrailerYardBundle? {
        val hit = (cache.get(code) as LegacyTrailerYardBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerYardBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerYardBundle> = inner.list()
}

fun newCachedLegacyTrailerYardBundleStore(inner: LegacyTrailerYardBundleStore, ttl: Int, clock: Clock): LegacyTrailerYardBundleStore {
    return CachedLegacyTrailerYardBundleStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerYardBundle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer yard bundles. Empty fields match all. */
data class LegacyTrailerYardBundleQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerYardBundle): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerYardBundle>): MutableList<LegacyTrailerYardBundle> {
        val matched = filter(items) { candidate: LegacyTrailerYardBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerYardBundles(store: LegacyTrailerYardBundleStore, query: LegacyTrailerYardBundleQuery): MutableList<LegacyTrailerYardBundle> {
    return query.apply(store.list())
}
