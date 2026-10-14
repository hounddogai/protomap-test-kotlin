package com.acme.transport.freightpay.audit.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy audit manifests by code. */
interface LegacyAuditManifestStore {
    fun find(code: String): LegacyAuditManifest?
    fun save(item: LegacyAuditManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyAuditManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyAuditManifestStore(
    var items: MutableMap<String, LegacyAuditManifest>,
    var order: MutableList<String>,
) : LegacyAuditManifestStore {
    override fun find(code: String): LegacyAuditManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyAuditManifest) {
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

    override fun list(): MutableList<LegacyAuditManifest> {
        val out = mutableListOf<LegacyAuditManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyAuditManifestStore(): LegacyAuditManifestStore {
    return MemoryLegacyAuditManifestStore(mutableMapOf<String, LegacyAuditManifest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyAuditManifestStore(
    var inner: LegacyAuditManifestStore,
    var cache: TtlCache<LegacyAuditManifest>,
    var hits: Int,
    var misses: Int,
) : LegacyAuditManifestStore {
    override fun find(code: String): LegacyAuditManifest? {
        val hit = (cache.get(code) as LegacyAuditManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyAuditManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyAuditManifest> = inner.list()
}

fun newCachedLegacyAuditManifestStore(inner: LegacyAuditManifestStore, ttl: Int, clock: Clock): LegacyAuditManifestStore {
    return CachedLegacyAuditManifestStore(
        inner = inner,
        cache = newTtlCache<LegacyAuditManifest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy audit manifests. Empty fields match all. */
data class LegacyAuditManifestQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyAuditManifest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<LegacyAuditManifest>): MutableList<LegacyAuditManifest> {
        val matched = filter(items) { candidate: LegacyAuditManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyAuditManifests(store: LegacyAuditManifestStore, query: LegacyAuditManifestQuery): MutableList<LegacyAuditManifest> {
    return query.apply(store.list())
}
