package com.acme.transport.freightpay.accessorial.manifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial manifests by code. */
interface AccessorialManifestStore {
    fun find(code: String): AccessorialManifest?
    fun save(item: AccessorialManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialManifestStore(
    var items: MutableMap<String, AccessorialManifest>,
    var order: MutableList<String>,
) : AccessorialManifestStore {
    override fun find(code: String): AccessorialManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialManifest) {
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

    override fun list(): MutableList<AccessorialManifest> {
        val out = mutableListOf<AccessorialManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialManifestStore(): AccessorialManifestStore {
    return MemoryAccessorialManifestStore(mutableMapOf<String, AccessorialManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialManifestStore(
    var inner: AccessorialManifestStore,
    var cache: TtlCache<AccessorialManifest>,
    var hits: Int,
    var misses: Int,
) : AccessorialManifestStore {
    override fun find(code: String): AccessorialManifest? {
        val hit = (cache.get(code) as AccessorialManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialManifest> = inner.list()
}

fun newCachedAccessorialManifestStore(inner: AccessorialManifestStore, ttl: Int, clock: Clock): AccessorialManifestStore {
    return CachedAccessorialManifestStore(
        inner = inner,
        cache = newTtlCache<AccessorialManifest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial manifests. Empty fields match all. */
data class AccessorialManifestQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialManifest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AccessorialManifest>): MutableList<AccessorialManifest> {
        val matched = filter(items) { candidate: AccessorialManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialManifests(store: AccessorialManifestStore, query: AccessorialManifestQuery): MutableList<AccessorialManifest> {
    return query.apply(store.list())
}
