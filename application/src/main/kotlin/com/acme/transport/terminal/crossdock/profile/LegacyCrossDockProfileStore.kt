package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy cross dock profiles by code. */
interface LegacyCrossDockProfileStore {
    fun find(code: String): LegacyCrossDockProfile?
    fun save(item: LegacyCrossDockProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCrossDockProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCrossDockProfileStore(
    var items: MutableMap<String, LegacyCrossDockProfile>,
    var order: MutableList<String>,
) : LegacyCrossDockProfileStore {
    override fun find(code: String): LegacyCrossDockProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCrossDockProfile) {
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

    override fun list(): MutableList<LegacyCrossDockProfile> {
        val out = mutableListOf<LegacyCrossDockProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCrossDockProfileStore(): LegacyCrossDockProfileStore {
    return MemoryLegacyCrossDockProfileStore(
        mutableMapOf<String, LegacyCrossDockProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCrossDockProfileStore(
    var inner: LegacyCrossDockProfileStore,
    var cache: TtlCache<LegacyCrossDockProfile>,
    var hits: Int,
    var misses: Int,
) : LegacyCrossDockProfileStore {
    override fun find(code: String): LegacyCrossDockProfile? {
        val hit = (cache.get(code) as LegacyCrossDockProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCrossDockProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCrossDockProfile> = inner.list()
}

fun newCachedLegacyCrossDockProfileStore(inner: LegacyCrossDockProfileStore, ttl: Int, clock: Clock): LegacyCrossDockProfileStore {
    return CachedLegacyCrossDockProfileStore(
        inner = inner,
        cache = newTtlCache<LegacyCrossDockProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cross dock profiles. Empty fields match all. */
data class LegacyCrossDockProfileQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCrossDockProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCrossDockProfile>): MutableList<LegacyCrossDockProfile> {
        val matched = filter(items) { candidate: LegacyCrossDockProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCrossDockProfiles(store: LegacyCrossDockProfileStore, query: LegacyCrossDockProfileQuery): MutableList<LegacyCrossDockProfile> {
    return query.apply(store.list())
}
