package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps rolling hub baselines by code. */
interface RollingHubBaselineStore {
    fun find(code: String): RollingHubBaseline?
    fun save(item: RollingHubBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingHubBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingHubBaselineStore(
    var items: MutableMap<String, RollingHubBaseline>,
    var order: MutableList<String>,
) : RollingHubBaselineStore {
    override fun find(code: String): RollingHubBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingHubBaseline) {
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

    override fun list(): MutableList<RollingHubBaseline> {
        val out = mutableListOf<RollingHubBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingHubBaselineStore(): RollingHubBaselineStore {
    return MemoryRollingHubBaselineStore(mutableMapOf<String, RollingHubBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingHubBaselineStore(
    var inner: RollingHubBaselineStore,
    var cache: TtlCache<RollingHubBaseline>,
    var hits: Int,
    var misses: Int,
) : RollingHubBaselineStore {
    override fun find(code: String): RollingHubBaseline? {
        val hit = (cache.get(code) as RollingHubBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingHubBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingHubBaseline> = inner.list()
}

fun newCachedRollingHubBaselineStore(inner: RollingHubBaselineStore, ttl: Int, clock: Clock): RollingHubBaselineStore {
    return CachedRollingHubBaselineStore(
        inner = inner,
        cache = newTtlCache<RollingHubBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling hub baselines. Empty fields match all. */
data class RollingHubBaselineQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingHubBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingHubBaseline>): MutableList<RollingHubBaseline> {
        val matched = filter(items) { candidate: RollingHubBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingHubBaselines(store: RollingHubBaselineStore, query: RollingHubBaselineQuery): MutableList<RollingHubBaseline> {
    return query.apply(store.list())
}
