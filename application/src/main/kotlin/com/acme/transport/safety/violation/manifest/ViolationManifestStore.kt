package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation manifests by code. */
interface ViolationManifestStore {
    fun find(code: String): ViolationManifest?
    fun save(item: ViolationManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationManifestStore(
    var items: MutableMap<String, ViolationManifest>,
    var order: MutableList<String>,
) : ViolationManifestStore {
    override fun find(code: String): ViolationManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationManifest) {
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

    override fun list(): MutableList<ViolationManifest> {
        val out = mutableListOf<ViolationManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationManifestStore(): ViolationManifestStore {
    return MemoryViolationManifestStore(mutableMapOf<String, ViolationManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationManifestStore(
    var inner: ViolationManifestStore,
    var cache: TtlCache<ViolationManifest>,
    var hits: Int,
    var misses: Int,
) : ViolationManifestStore {
    override fun find(code: String): ViolationManifest? {
        val hit = (cache.get(code) as ViolationManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationManifest> = inner.list()
}

fun newCachedViolationManifestStore(inner: ViolationManifestStore, ttl: Int, clock: Clock): ViolationManifestStore {
    return CachedViolationManifestStore(
        inner = inner,
        cache = newTtlCache<ViolationManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation manifests. Empty fields match all. */
data class ViolationManifestQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationManifest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationManifest>): MutableList<ViolationManifest> {
        val matched = filter(items) { candidate: ViolationManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationManifests(store: ViolationManifestStore, query: ViolationManifestQuery): MutableList<ViolationManifest> {
    return query.apply(store.list())
}
