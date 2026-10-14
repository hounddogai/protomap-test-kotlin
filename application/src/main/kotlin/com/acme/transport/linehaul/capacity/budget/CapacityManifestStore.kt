package com.acme.transport.linehaul.capacity.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity manifests by code. */
interface CapacityManifestStore {
    fun find(code: String): CapacityManifest?
    fun save(item: CapacityManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityManifestStore(
    var items: MutableMap<String, CapacityManifest>,
    var order: MutableList<String>,
) : CapacityManifestStore {
    override fun find(code: String): CapacityManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityManifest) {
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

    override fun list(): MutableList<CapacityManifest> {
        val out = mutableListOf<CapacityManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityManifestStore(): CapacityManifestStore {
    return MemoryCapacityManifestStore(mutableMapOf<String, CapacityManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityManifestStore(
    var inner: CapacityManifestStore,
    var cache: TtlCache<CapacityManifest>,
    var hits: Int,
    var misses: Int,
) : CapacityManifestStore {
    override fun find(code: String): CapacityManifest? {
        val hit = (cache.get(code) as CapacityManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityManifest> = inner.list()
}

fun newCachedCapacityManifestStore(inner: CapacityManifestStore, ttl: Int, clock: Clock): CapacityManifestStore {
    return CachedCapacityManifestStore(
        inner = inner,
        cache = newTtlCache<CapacityManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity manifests. Empty fields match all. */
data class CapacityManifestQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityManifest): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<CapacityManifest>): MutableList<CapacityManifest> {
        val matched = filter(items) { candidate: CapacityManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityManifests(store: CapacityManifestStore, query: CapacityManifestQuery): MutableList<CapacityManifest> {
    return query.apply(store.list())
}
