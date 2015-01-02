/*******************************************************************************
 *   Gisgraphy Project 
 * 
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 * 
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *   Lesser General Public License for more details.
 * 
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA
 * 
 *  Copyright 2008  Gisgraphy project 
 *  David Masclet <davidmasclet@gisgraphy.com>
 *  
 *  
 *******************************************************************************/
package com.gisgraphy.domain.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Required;

import com.gisgraphy.domain.geoloc.entity.Adm;
import com.gisgraphy.domain.geoloc.entity.AlternateName;
import com.gisgraphy.domain.geoloc.entity.City;
import com.gisgraphy.domain.geoloc.entity.GisFeature;
import com.gisgraphy.domain.geoloc.entity.ZipCode;
import com.gisgraphy.domain.valueobject.AlternateNameSource;
import com.gisgraphy.domain.valueobject.GisFeatureDistance;
import com.gisgraphy.fulltext.AbstractIntegrationHttpSolrTestCase;
import com.gisgraphy.helper.GeolocHelper;
import com.gisgraphy.importer.ImporterConfig;
import com.gisgraphy.test.GisgraphyTestHelper;
import com.vividsolutions.jts.geom.Geometry;

public class GisFeatureDaoTest extends AbstractIntegrationHttpSolrTestCase {

    private IGisFeatureDao gisFeatureDao;

    private ICityDao cityDao;

    private IAdmDao admDao;

    private ICountryDao countryDao;

    private IAlternateNameDao alternateNameDao;

    @Resource
    private GisgraphyTestHelper geolocTestHelper;

    /*
     * test delete
     */

    @Test
    public void testRemoveWithNullShouldThrows() {
	try {
	    this.gisFeatureDao.remove(null);
	    fail();
	} catch (IllegalArgumentException e) {
	}
    }

    @Test
    public void testRemoveACityWithGisFeatureDaoShouldRemoveTheCityAndTheInheritedGisFeature() {

	// expected : the city is removed

	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, new Random().nextLong());
	City paris = new City(gisFeature);
	// save city
	City savedParis = this.cityDao.save(paris);
	// chek city is well saved
	Long id = savedParis.getId();
	City retrievedParis = this.cityDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(paris.getId(), retrievedParis.getId());

	// remove city
	this.gisFeatureDao.remove(retrievedParis);

	// check city is removed
	City retrievedParisafterRemove = this.cityDao.get(id);
	assertEquals(null, retrievedParisafterRemove);

	// check gisFeature is remove
	GisFeature savedGisFeatureafterRemove = this.gisFeatureDao.get(id);
	assertNull(savedGisFeatureafterRemove);
    }

    @Test
    public void testRemoveACityCastInGisFeatureWithGisFeatureDaoShouldREmoveTheCity() {
	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, new Random().nextLong());
	City paris = new City(gisFeature);

	// save city
	City savedParis = this.cityDao.save(paris);

	// chek city is well saved
	Long id = savedParis.getId();
	City retrievedParis = this.cityDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(paris.getId(), retrievedParis.getId());

	// remove city
	this.gisFeatureDao.remove((GisFeature) retrievedParis);
	// savedParis.setGisFeature(null);
	// check city is removed

	City retrievedParisafterRemove = this.cityDao.get(id);
	assertEquals(null, retrievedParisafterRemove);

	// check gisFeature is remove
	GisFeature savedGisFeatureafterRemove = this.gisFeatureDao.get(id);
	assertNull(savedGisFeatureafterRemove);

    }

    @Test
    public void testRemoveGisFeatureWhichIsAnAdm2() {
	// see admDaotest.testDeleteAdmShouldDeleteAdm()
    }

    @Test
    public void testDeleteAllListShouldThrowsIfListIsNull() {
	try {
	    this.gisFeatureDao.deleteAll(null);
	    fail();
	} catch (IllegalArgumentException e) {

	}
    }

    @Test
    public void testDeleteAllListShouldNotThrowsForAnEmptyList() {
	try {
	    this.gisFeatureDao.deleteAll(new ArrayList<GisFeature>());

	} catch (IllegalArgumentException e) {
	    fail();
	}
    }

    @Test
    public void testDeleteAllListShouldDeleteTheSpecifiedElements() {
	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, new Random().nextLong());
	City paris = new City(gisFeature);

	GisFeature gisFeature2 = GisgraphyTestHelper.createCity("cityGisFeature2",
		null, null, new Random().nextLong());
	City paris2 = new City(gisFeature2);

	// save cities
	City savedParis = this.cityDao.save(paris);
	City savedParis2 = this.cityDao.save(paris2);

	// chek cities are well saved
	City retrievedParis = this.cityDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(savedParis.getId(), retrievedParis.getId());

	City retrievedParis2 = this.cityDao.get(savedParis2.getId());
	assertNotNull(retrievedParis2);
	assertEquals(savedParis2.getId(), retrievedParis2.getId());

	List<GisFeature> listToDelete = new ArrayList<GisFeature>();
	listToDelete.add(retrievedParis);
	this.gisFeatureDao.deleteAll(listToDelete);

	List<GisFeature> stillStored = this.gisFeatureDao.getAll();
	assertNotNull(stillStored);
	assertEquals(1, stillStored.size());
	assertEquals(savedParis2, stillStored.get(0));

    }

    @Test
    public void testDeleteALLShouldDeleteAlltheElements() {
	GisFeature paris = GisgraphyTestHelper.createGisFeature("GisFeature",
		null, null, new Random().nextLong());

	GisFeature gisFeature2 = GisgraphyTestHelper.createCity("cityGisFeature2",
		null, null, new Random().nextLong());
	City paris2 = new City(gisFeature2);

	// save cities
	GisFeature savedParis = this.gisFeatureDao.save(paris);
	City savedParis2 = this.cityDao.save(paris2);

	// chek cities are well saved
	GisFeature retrievedParis = this.gisFeatureDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(savedParis.getId(), retrievedParis.getId());

	City retrievedParis2 = this.cityDao.get(savedParis2.getId());
	assertNotNull(retrievedParis2);
	assertEquals(savedParis2.getId(), retrievedParis2.getId());

	assertEquals(1, this.cityDao.deleteAll());

	List<City> stillStoredCity = this.cityDao.getAll();
	assertNotNull(stillStoredCity);
	assertEquals(0, stillStoredCity.size());

	List<GisFeature> stillStoredGis = this.gisFeatureDao.getAll();
	assertNotNull(stillStoredGis);
	assertEquals(1, stillStoredGis.size());
    }

    @Test
    public void testDeleteAdmShouldNotDeleteTheGisFeaturesContainedInCascade() {
	// save Adm
	Adm adm = GisgraphyTestHelper.createAdm("adm", "FR", "A1", "B2", "C3",
		"D4", null, 4);
	Adm savedAdm = this.admDao.save(adm);
	assertNotNull(savedAdm.getId());
	// check adm1 is saved
	Adm retrievedAdm = this.admDao.get(savedAdm.getId());
	assertEquals(savedAdm, retrievedAdm);
	assertEquals(savedAdm.getId(), retrievedAdm.getId());

	// creategisFeatureand set his Adm
	GisFeature gisFeature = GisgraphyTestHelper.createCity("paris", 1.3F, 45F,
		null);
	gisFeature.setAdm(retrievedAdm);

	// save gisFeature
	GisFeature savedGisFeature = gisFeatureDao.save(gisFeature);

	// check it is saved
	GisFeature retrievedGisFeature = this.gisFeatureDao.get(savedGisFeature
		.getId());
	assertNotNull(retrievedGisFeature);
	assertEquals(savedGisFeature, retrievedGisFeature);

    }

    @Test
    public void testDeleteAllExceptAdmAndCountries() {
	geolocTestHelper.createAndSaveCityWithFullAdmTreeAndCountry(3L);
	GisFeature gisFeatureWithNullFeatureCode = GisgraphyTestHelper
		.createGisFeature("gis", 3F, 4F, 4L);
	gisFeatureDao.save(gisFeatureWithNullFeatureCode);
	GisFeature gisFeatureWithNotNullFeatureCode = GisgraphyTestHelper
		.createGisFeature("gis", 3F, 4F, 5L);
	gisFeatureWithNotNullFeatureCode.setFeatureClass("A");
	gisFeatureWithNotNullFeatureCode.setFeatureCode("B");
	gisFeatureDao.save(gisFeatureWithNotNullFeatureCode);
	// check 3 adm, country,city, and gisFeature are saved
	assertEquals(1, countryDao.count());
	assertEquals(7, gisFeatureDao.count());
	// 1 city + 1 gis with null FeatureCode + 1 GIS with not null featureCode
	assertEquals(3, gisFeatureDao.deleteAllExceptAdmsAndCountries());
	assertEquals(3, admDao.count());
	assertEquals(1, countryDao.count());
	// 3 adm + 1 country
	assertEquals(4, gisFeatureDao.count());
	assertEquals(1, countryDao.count());
	assertEquals(0, cityDao.count());

    }

    /*
     * test save
     */

    @Test
    public void testSaveCityCastInGisFeatureShouldSaveTheCity() {
	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, new Random().nextLong());
	City paris = new City(gisFeature);
	// save city
	GisFeature savedParis = this.gisFeatureDao.save((GisFeature) paris);
	// chek city is well saved
	savedParis.getId();
	City retrievedParis = this.cityDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(paris.getId(), retrievedParis.getId());
    }

    @Test
    public void testSaveWithNullShouldThrows() {
	try {
	    this.gisFeatureDao.save(null);
	    fail();
	} catch (IllegalArgumentException e) {
	}
    }

    @Test
    public void testSaveCityWithGisFeatureDaoShouldSaveTheCity() {
	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, new Random().nextLong());
	City paris = new City(gisFeature);
	// save city
	GisFeature savedParis = this.gisFeatureDao.save(paris);
	// chek city is well saved
	City retrievedParis = this.cityDao.get(savedParis.getId());
	assertNotNull(retrievedParis);
	assertEquals(paris.getId(), retrievedParis.getId());
    }

    @Test
    public void testSaveShouldSaveTheAlternateNamesInCascade() {
	int nbalternateNames = 3;
	GisFeature gisFeature = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("paris", nbalternateNames);
	assertNotNull(gisFeature.getAlternateNames());
	assertEquals(3, gisFeature.getAlternateNames().size());
	GisFeature saved = this.gisFeatureDao.save(gisFeature);
	GisFeature retrieved = this.gisFeatureDao.get(saved.getId());
	assertNotNull(retrieved);
	assertEquals(gisFeature.getId(), retrieved.getId());
	assertNotNull(retrieved.getAlternateNames());
	assertEquals(nbalternateNames, retrieved.getAlternateNames().size());
    }

    // test get

    @Test
    public void testgetNearestAndDistanceFromGisFeatureShouldTakeTheSpecifiedClassIntoAccount() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.cityDao.save(p1);
	this.cityDao.save(p2);
	this.cityDao.save(p3);

	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFromGisFeature(p1, 1000000,true,
			GisFeature.class);
	assertEquals(2, results.size());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000,true, City.class);
	assertEquals(2, results.size());

	GisFeature p4 = GisgraphyTestHelper.createGisFeature("test", 49.01668F,
		2.46667F, 4L);
	this.gisFeatureDao.save(p4);

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, true,GisFeature.class);
	assertEquals(3, results.size());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000,true, City.class);
	assertEquals(2, results.size());

    }

    @Test
    public void testgetNearestAndDistanceFromGisFeatureShouldThrowsIfGisFeatureIsNull() {

	try {
	    this.gisFeatureDao.getNearestAndDistanceFromGisFeature(null,
		    1000000,true, GisFeature.class);
	    fail("getNearestAndDistanceFromGisFeature should throws if gisFeature is null");
	} catch (IllegalArgumentException e) {

	}

    }

    @Test
    public void testgetNearestAndDistanceFromGisFeatureShouldfilterMunicipality() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	p2.setMunicipality(true);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
	// for city dao
	List<GisFeatureDistance> results  = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 0, 10, true,City.class, false);
	assertEquals(2, results.size());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 0, 10, true,City.class, true);
	assertEquals(1, results.size());
	assertEquals(p2.getName(), results.get(0).getName());

    }
    
    @Test
    public void testgetNearestAndDistanceFromGisFeatureShouldPaginate() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
	// for city dao
	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFromGisFeature(p1, 1000000, 1, 5,true);
	assertEquals(2, results.size());
	assertEquals(p3.getName(), results.get(0).getName());
	assertEquals(p2.getName(), results.get(1).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 2, 5, true,City.class, false);
	assertEquals(1, results.size());
	assertEquals(p2.getName(), results.get(0).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 1, 1, true,City.class, false);
	assertEquals(1, results.size());
	assertEquals(p3.getName(), results.get(0).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 0, 1,true, City.class, false);
	assertEquals(1, results.size());
	assertEquals(p3.getName(), results.get(0).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 1, 0,true, City.class, false);
	assertEquals(2, results.size());
	assertEquals(p3.getName(), results.get(0).getName());
	assertEquals(p2.getName(), results.get(1).getName());

    }
    
    @Test
    public void testgetNearestAndDistanceFromGisFeatureShouldNotCalculateTheDistanceIfincludeDistanceFieldIsFalse() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
	// for city dao
	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFromGisFeature(p1, 1000000, 1, 5,true);
	assertEquals(2, results.size());
	assertEquals(p3.getName(), results.get(0).getName());
	assertEquals(p2.getName(), results.get(1).getName());


	results = this.gisFeatureDao.getNearestAndDistanceFromGisFeature(p1,
		1000000, 1, 0, false, City.class, false);
	assertEquals(2, results.size());
	
	assertNull("The distance should be null if includeDistanceField is false", results.get(0).getDistance());
	assertNull("The distance should be null if includeDistanceField is false", results.get(1).getDistance());

    }

    @Test
    public void testgetNearestAndDistanceFromShouldTakeTheSpecifiedClassIntoAccount() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.cityDao.save(p1);
	this.cityDao.save(p2);
	this.cityDao.save(p3);

	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFrom(p1.getLocation(), 1000000,true,
			GisFeature.class);
	assertEquals(3, results.size());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, true,City.class);
	assertEquals(3, results.size());

	GisFeature p4 = GisgraphyTestHelper.createGisFeature("test", 49.01668F,
		2.46667F, 4L);
	this.gisFeatureDao.save(p4);

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, true,GisFeature.class);
	assertEquals(4, results.size());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, true, City.class);
	assertEquals(3, results.size());

    }
    
    @Test
    public void testgetNearestAndDistanceFromShouldFilterIsMunicipality() {
    	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
    	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
    		3L);
    	p2.setMunicipality(true);
    	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
    		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
	// for city dao
	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFrom(p1.getLocation(), 1000000, 1, 5, true, false);
	assertEquals(3, results.size());
	assertEquals(p1.getName(), results.get(0).getName());
	assertEquals(p3.getName(), results.get(1).getName());
	assertEquals(p2.getName(), results.get(2).getName());

	results = this.gisFeatureDao
			.getNearestAndDistanceFrom(p1.getLocation(), 1000000, 1, 5, true, true);
	assertEquals(1, results.size());
	assertEquals(p2.getName(), results.get(0).getName());

    }

    @Test
    public void testgetNearestAndDistanceFromShouldPaginate() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
	// for city dao
	List<GisFeatureDistance> results = this.gisFeatureDao
		.getNearestAndDistanceFrom(p1.getLocation(), 1000000, 1, 5, true, false);
	assertEquals(3, results.size());
	assertEquals(p1.getName(), results.get(0).getName());
	assertEquals(p3.getName(), results.get(1).getName());
	assertEquals(p2.getName(), results.get(2).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, 2, 5, true,City.class);
	assertEquals(2, results.size());
	assertEquals(p3.getName(), results.get(0).getName());
	assertEquals(p2.getName(), results.get(1).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, 1, 1, true,City.class);
	assertEquals(1, results.size());
	assertEquals(p1.getName(), results.get(0).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, 0, 1,true, City.class);
	assertEquals(1, results.size());
	assertEquals(p1.getName(), results.get(0).getName());

	results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, 1, 0, true,City.class);
	assertEquals(3, results.size());
	assertEquals(p1.getName(), results.get(0).getName());
	assertEquals(p3.getName(), results.get(1).getName());
	assertEquals(p2.getName(), results.get(2).getName());

    }
    
    
    @Test
    public void testgetNearestAndDistanceFromhouldNotCalculateTheDistanceIfincludeDistanceFieldIsFalse() {
	City p1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F, 1L);
	City p2 = GisgraphyTestHelper.createCity("bordeaux", 44.83333F, -0.56667F,
		3L);
	City p3 = GisgraphyTestHelper.createCity("goussainville", 49.01667F,
		2.46667F, 2L);

	this.gisFeatureDao.save(p1);
	this.gisFeatureDao.save(p2);
	this.gisFeatureDao.save(p3);
		

	List<GisFeatureDistance> results = this.gisFeatureDao.getNearestAndDistanceFrom(
		p1.getLocation(), 1000000, 0, 4, false,City.class);
	assertEquals(3, results.size());
	assertNull("The distance should be null if includeDistanceField is false", results.get(0).getDistance());
	assertNull("The distance should be null if includeDistanceField is false", results.get(1).getDistance());
	assertNull("The distance should be null if includeDistanceField is false", results.get(2).getDistance());

    }

    @Test
    public void testGetEagerShouldLoadAlternateNamesAndAdm() {
	Long featureId = 1001L;
	City gisFeature = GisgraphyTestHelper.createCity("Saint-André", 1.5F, 2F,
		featureId);
	AlternateName alternateName = new AlternateName();
	alternateName.setName("alteré");
	alternateName.setGisFeature(gisFeature);
	alternateName.setSource(AlternateNameSource.ALTERNATENAMES_FILE);
	gisFeature.addAlternateName(alternateName);
	City paris = new City(gisFeature);
	paris.addZipCode(new ZipCode("50263"));

	Adm admParent = GisgraphyTestHelper.createAdm("admparent", "FR", "A1",
		"B2", "C3", null, null, 3);

	Adm p = this.admDao.save(admParent);
	paris.setAdm(p);

	this.cityDao.save(paris);
	this.cityDao.flushAndClear();
	this.admDao.flushAndClear();
	// test in non eagerMode
	City retrieved = this.cityDao.get(paris.getId());
	this.cityDao.flushAndClear();
	this.admDao.flushAndClear();
	try {
	    assertEquals(1, retrieved.getAlternateNames().size());
	    fail("without eager mode, getAlternateNames should throw");
	} catch (RuntimeException e) {

	}
	try {
	    assertEquals("C3", retrieved.getAdm().getAdm3Code());
	    fail("without eager mode, getAdm should throw");
	} catch (RuntimeException e) {
	}
	// test In Eager Mode
	retrieved = this.cityDao.getEager(paris.getId());
	this.cityDao.flushAndClear();
	this.admDao.flushAndClear();
	try {
	    assertEquals(1, retrieved.getAlternateNames().size());
	} catch (RuntimeException e) {
	    fail("with eager mode, getAlternateNames should not throw");
	}
	try {
	    assertEquals("C3", retrieved.getAdm().getAdm3Code());
	} catch (RuntimeException e) {
	    fail("with eager mode, getAdm should not throw");
	}
    }

    @Test
    public void testlistAllFeaturesFromTextShouldReturnResults() {
	City city = GisgraphyTestHelper.createCityAtSpecificPoint("sèvres", 1.5F,
		1.6F);
	city.setFeatureId(3L);
	this.gisFeatureDao.save(city);
	this.solRSynchroniser.commit();
	this.solRSynchroniser.optimize();// to avoid too many open files
	// http://grep.codeconsult.ch/2006/07/18/lucene-too-many-open-files-explained/
	List<GisFeature> results = this.gisFeatureDao.listAllFeaturesFromText(
		"sèvres", true);
	assertNotNull("The fulltext search engine should not return null",
		results);
	assertTrue("size should be 1 but is " + results.size(),
		results.size() == 1);
	assertEquals(
		"The fulltext search engine does not return the expected result",
		new Long(3), results.get(0).getFeatureId());

    }

    @Test
    public void testGetShouldThrowsIfIdIsNull() {
	try {
	    this.gisFeatureDao.get(null);
	    fail();
	} catch (IllegalArgumentException e) {
	}
    }

    @Test
    public void testExistShouldThrowsIfIdIsNull() {
	try {
	    this.gisFeatureDao.exists(null);
	    fail();
	} catch (IllegalArgumentException e) {
	}
    }

    @Test
    public void testGetAllPaginateShouldNeverReturnNull() {
	assertNotNull(this.gisFeatureDao.getAllPaginate(1, 5));
    }

    @Test
    public void testGetByFeatureIdShouldRetrieveTheCorrectGisfeature() {
	Long featureId = 1001L;
	GisFeature gisFeature = GisgraphyTestHelper.createCity("gisfeatureName",
		null, null, featureId);
	GisFeature savedGisFeature = this.gisFeatureDao.save(gisFeature);
	GisFeature retrievedGisFeature = this.gisFeatureDao
		.getByFeatureId(featureId);
	assertNotNull(retrievedGisFeature);
	assertEquals(retrievedGisFeature.getId(), savedGisFeature.getId());
	assertEquals(retrievedGisFeature, savedGisFeature);
    }

    @Test
    public void testGetByFeatureIdShouldRetrieveACityIfTheGisfeatureIsAcity() {
	Long featureId = 1001L;
	GisFeature gisFeature = GisgraphyTestHelper.createCity("cityGisFeature",
		null, null, featureId);
	City paris = new City(gisFeature);
	// save city
	this.gisFeatureDao.save(paris);
	// chek city is well saved and getByFeatureID Return an object of City
	// Type
	GisFeature retrievedParis = this.gisFeatureDao
		.getByFeatureId(featureId);
	assertNotNull(retrievedParis);
	assertEquals(paris.getId(), retrievedParis.getId());
	assertEquals(City.class, retrievedParis.getClass());

    }

    @Test
    public void testgetDirtyShouldRetieveDirtyGisFeature() {
	// create two gis Feature
	GisFeature gisFeature1 = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("paris", 3);
	GisFeature gisFeature2 = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("paris2", 3);
	GisFeature gisFeature3 = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("paris3", 3);
	GisFeature gisFeature4 = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("paris4", 3);
	gisFeature2.setFeatureCode(ImporterConfig.DEFAULT_FEATURE_CODE);
	gisFeature4.setFeatureClass(ImporterConfig.DEFAULT_FEATURE_CLASS);
	gisFeature3.setLocation(GisgraphyTestHelper.createPoint(0F, 0F));

	// save
	this.gisFeatureDao.save(gisFeature1);
	this.gisFeatureDao.save(gisFeature2);
	this.gisFeatureDao.save(gisFeature3);
	this.gisFeatureDao.save(gisFeature4);

	// check it is well saved
	List<GisFeature> gisFeatures = this.gisFeatureDao.getAll();
	assertNotNull(gisFeatures);
	assertEquals(4, gisFeatures.size());

	List<GisFeature> dirties = this.gisFeatureDao.getDirties();
	assertNotNull(dirties);
	assertEquals(3, dirties.size());
	// assertEquals(gisFeature2, dirties.get(0));

    }

    @Test
    public void testgetDirtyShouldNeverReturnNull() {
	assertNotNull(this.gisFeatureDao.getDirties());
    }

    public void testGetByFeatureIdsShouldReturnTheGisFeature() {
	City city1 = GisgraphyTestHelper.createCity("cityGisFeature", null, null,
		100L);
	City city2 = GisgraphyTestHelper.createCity("cityGisFeature", null, null,
		200L);
	GisFeature gisFeature = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("gisfeature", 0);
	gisFeature.setFeatureId(300L);
	GisFeature gisFeature2 = GisgraphyTestHelper
		.createGisFeatureWithAlternateNames("gisfeature", 0);
	gisFeature2.setFeatureId(400L);

	this.gisFeatureDao.save(city1);
	this.gisFeatureDao.save(city2);
	this.gisFeatureDao.save(gisFeature);
	this.gisFeatureDao.save(gisFeature2);

	// check it is well saved
	List<GisFeature> gisFeatures = this.gisFeatureDao.getAll();
	assertNotNull(gisFeatures);
	assertEquals(4, gisFeatures.size());

	List<Long> ids = new ArrayList<Long>();
	ids.add(100L);
	ids.add(200L);
	ids.add(300L);
	List<GisFeature> gisByIds = this.gisFeatureDao.listByFeatureIds(ids);
	assertNotNull(gisByIds);
	assertEquals(3, gisByIds.size());

    }

    @Test
    public void testGetByFeatureIdsWithANullListOrEmptyListShouldReturnEmptylist() {
	// test with null
	List<GisFeature> results = this.gisFeatureDao.listByFeatureIds(null);
	assertNotNull(results);
	assertEquals(0, results.size());

	// test with an empty list
	results = this.gisFeatureDao.listByFeatureIds(new ArrayList<Long>());
	assertNotNull(results);
	assertEquals(0, results.size());

    }

    @Test
    public void testGetByFeatureIdWithANullFeatureIdShouldThrows() {
	try {
	    this.gisFeatureDao.getByFeatureId(null);
	    fail();
	} catch (IllegalArgumentException e) {
	}

    }

    @Test
    public void testSetFlushModeWithNullShouldThrows() {
	try {
	    this.gisFeatureDao.setFlushMode(null);
	    fail();
	} catch (RuntimeException e) {
	}
    }

    @Test
    public void testGetAllShouldNotReturnNull() {
	assertNotNull(gisFeatureDao.getAll());
    }

    @Test
    public void testListByNameShouldNotReturnNullButAnEmptyList() {
	assertNotNull(gisFeatureDao.listByName("ABC"));
    }

    @Test
    public void testListByNameWithNullNameShouldThrows() {
	try {
	    assertNotNull(gisFeatureDao.listByName(null));
	    fail();
	} catch (IllegalArgumentException e) {
	}
    }

    public void testListFromTextShouldOnlyReturnFeaturesOfTheSpecifiedClass() {
	// create one city
	Long featureId = 1001L;
	GisFeature gisFeature = GisgraphyTestHelper.createCity("Saint-André",
		1.5F, 2F, featureId);
	AlternateName alternateName = new AlternateName();
	alternateName.setName("alteré");
	alternateName.setGisFeature(gisFeature);
	alternateName.setSource(AlternateNameSource.ALTERNATENAMES_FILE);
	gisFeature.addAlternateName(alternateName);
	City paris = new City(gisFeature);
	paris.addZipCode(new ZipCode("50263"));

	// create ADM
	GisFeature gisAdm = GisgraphyTestHelper.createGisFeatureForAdm(
		"Saint-andré", 2.5F, 3.5F, 40L, 4);
	Adm adm = GisgraphyTestHelper.createAdm("Saint-André", "FR", "A1", "B2",
		"C3", "D4", gisAdm, 4);

	// create a second city
	Long featureId2 = 1002L;
	GisFeature gisFeature2 = GisgraphyTestHelper.createCity("mytown", 1.5F,
		2F, featureId2);
	City paris2 = new City(gisFeature2);
	paris2.addZipCode(new ZipCode("50264"));

	// save cities and check it is saved
	this.gisFeatureDao.save(paris);
	assertNotNull(this.gisFeatureDao.getByFeatureId(featureId));
	this.gisFeatureDao.save(paris2);
	assertNotNull(this.gisFeatureDao.getByFeatureId(featureId2));

	// save Adm and check it is saved
	this.admDao.save(adm);
	assertTrue(this.admDao.getAll().size() == 1);

	// check alternatename is saved
	assertEquals(1, alternateNameDao.getAll().size());

	// commit changes
	this.solRSynchroniser.commit();

	// exact name
	List<City> results = this.cityDao.listFromText("Saint-André", false);
	assertTrue(
		"There must only have one results and only one (the city one), the adm should not be retrieved an nor the second city",
		results.size() == 1);
	assertEquals("Saint-André", results.get(0).getName());
	assertTrue(results.get(0).getFeatureId() == featureId);

	// test synonyms
	results = this.cityDao.listFromText("st-André", false);
	assertTrue("The synonyms with st/saint/santa should be returned",
		results.size() == 1);
	assertEquals("Saint-André", results.get(0).getName());
	assertTrue(results.get(0).getFeatureId() == featureId);

	results = this.cityDao.listFromText("St-André", false);
	assertTrue("The synonyms must be case insensitive", results.size() == 1);
	assertEquals("Saint-André", results.get(0).getName());
	assertTrue(results.get(0).getFeatureId() == featureId);

	List<GisFeature> resultsGis = this.gisFeatureDao.listFromText(
		"Saint-André", false);
	assertTrue(
		"Even if gisFeature is a city, no gisFeature should be retrieved",
		resultsGis.size() == 0);

	// test ADM
	List<Adm> resultsAdm = this.admDao.listFromText("Saint-André", false);
	assertTrue("an Adm should be retrieved for Saint-André", resultsAdm
		.size() == 1);
	assertTrue(resultsAdm.get(0).getFeatureId() == 40L);

	// test other named
	List<Adm> noResults = this.admDao.listFromText("test", true);
	assertTrue("no Adm should be found for 'test'", noResults.size() == 0);
	List<GisFeature> noResults2 = this.gisFeatureDao.listFromText("test",
		true);
	assertTrue("no GisFeature should be found for 'test'", noResults2
		.size() == 0);
	List<City> noResults3 = this.cityDao.listFromText("test", true);
	assertTrue("no city should be found for 'test'", noResults3.size() == 0);

	// test zipcode
	List<Adm> zipResults = this.admDao.listFromText("50264", true);
	assertTrue("no Adm should be found for '50264'", zipResults.size() == 0);
	List<GisFeature> zipResults2 = this.gisFeatureDao.listFromText("50264",
		true);
	assertTrue("no GisFeature should be found for '50264'", zipResults2
		.size() == 0);
	List<City> zipResults3 = this.cityDao.listFromText("50264", true);
	assertTrue("a city should be found for '50264' in alternatenames",
		zipResults3.size() == 1);
	List<City> zipResults4 = this.cityDao.listFromText("50264", false);
	assertTrue("a city should be found for '50264' in zipcode", zipResults4
		.size() == 1);

	// test alternatenames should not be included
	results = this.cityDao.listFromText("alteré", false);
	assertTrue("alternateName should not be included", results.size() == 0);
	results = this.cityDao.listFromText("alteré", true);
	assertTrue("alternateName should be included", results.size() == 1);
	assertEquals("Saint-André", results.get(0).getName());
	assertTrue(results.get(0).getFeatureId() == featureId);

	// test fulltext engine
	results = this.cityDao.listFromText("Saint André", false);
	assertTrue(
		"the fulltext search engine should be iso, case and - insensitive",
		results.size() == 1);

	results = this.cityDao.listFromText("saInt andré", false);
	assertTrue(
		"the fulltext search engine should be iso, case and - insensitive",
		results.size() == 1);

	results = this.cityDao.listFromText("saInt andre", false);
	assertTrue(
		"the fulltext search engine should be iso, case and - insensitive",
		results.size() == 1);
    }
    
    @Test
    public void testGetMaxFeatureId(){
    long firstFeatureId = 10L;
	long featureId = 42L;
	GisFeature gisFeature = GisgraphyTestHelper.createGisFeature("asciiName", 1F, 2F, firstFeatureId);
	gisFeatureDao.save(gisFeature);
	long estimateCount = gisFeatureDao.getMaxFeatureId();
	Assert.assertEquals("getMaxFeatureId should return the max gid",firstFeatureId, estimateCount);
	
	City city = GisgraphyTestHelper.createCity("city", 3.5F, 4.2F, featureId);
	
	gisFeatureDao.save(city);
	estimateCount = gisFeatureDao.getMaxFeatureId();
	Assert.assertEquals("getMaxFeatureId should return the max gid of subtype",featureId, estimateCount);
	
	
    }
    
    @Test
    public void testCreateGISTIndexForLocationColumnShouldNotThrow(){
	gisFeatureDao.createGISTIndexForLocationColumn();
    }
    
    @Test
    public void testGetShapeAsWKTByFeatureId(){
    	City city1 = GisgraphyTestHelper.createCity("paris", 48.86667F, 2.3333F,
    			1L);
    	city1.setCountryCode("FR");
    	Geometry shape = GeolocHelper.convertFromHEXEWKBToGeometry("0103000020E6100000010000001C0200007E31A53F45FF1440038A479858C747406B5E7AA0BAFE1440E2AE5E4546C747409693F540D0FE14404F6B894B44C74740D272A087DAFE1440400CBE7A26C74740F7764B72C0FE1440D99EB4CB12C74740458C8D2F90FE1440EA70BE2209C74740A788C21D5EFE1440F3CF0CE203C74740F6622827DAFD1440B5746094FBC6474064C3E457BDFD1440E273CC1E0DC74740B23B93ECC7FC1440F37FA20D1BC74740044070F0E0FA144043CBBA7F2CC7474024FA10AF46F914409224085740C74740DFCD099057F614402175E04158C74740AD472B082FF31440236EA9DE75C74740DCEFF55A75F21440EF0D74A37EC74740CD199B6736F11440DFF9EA4F87C74740F401DC8717EF144022D390A79BC74740450603194BEA1440DB47B8DAD4C74740171EEA2DD4E81440D9E5A5ACF0C74740623DA4CE92E61440751EBA4505C847401B80B2CE9DE514403E1C6ED113C847406ED113E005E314409C3B551921C8474086CC9541B5E1144089F83DA022C8474005334BA71AE2144047BE011C31C84740D913B8D0F0E11440D90F0C7B35C847404F81824188E2144047D0F30247C84740F8883D59D6E214402F77C13B54C847407A162939CCDC1440729F6692A2C847408E36D8E5A5DC144024BA675DA3C847403727E5A4E6D91440D0D38041D2C84740E6A4E66157D914403609394AB9C8474055089A852CD914402450583DBBC84740D2B1DE03CFD81440D6C33CD0AFC8474037740EF9C2D714402B4C84B295C847403E51233EC1D71440C1594A9693C847407E9C1F35DCD61440105CE50984C84740BF07F9EAF4D51440333A7BC26DC84740FC500E0B5AD51440A320787C7BC84740663046240AD5144012DBDD0374C8474069DF81A6CAD414405FCE119E75C847408879A05FA5D41440749CDB847BC84740C09E0A13A1D41440A9AF9DDE7BC84740256E5F515BD4144012DE1E8480C84740139B34B2D0D31440FDD357A192C84740A1664815C5D314402C52BC2594C84740C0DB72E437D314408D5B71169BC8474071EBC9575DD214408850002082C8474053C83AC1A3D2144044A275F97BC84740B73DE6A848D21440F06C8FDE70C847401CE846FD9AD11440FD3D67C17AC84740114DEA1560D1144006E8AD7081C8474029B7483547D114408DA5A37785C8474099E02FC104D114402FD7EC9282C84740AAFAF087FAD01440874AC8AC83C84740654C0BA881D01440226239F878C84740FA0D130D52D01440616AF06371C84740E9E62CFD95CF1440380E61A17BC847403253FFC5D8CE1440A89432045BC8474008019E59B7CE14405640EB2B59C847403A9270C6D5CD1440B9A2395739C847409E52149E86CE144095BDA59C2FC847402E2DD96784CD1440D1C65BF80BC84740E81890BDDECD1440F10B546B06C84740E8C1DD59BBCD14403BFE0B0401C84740F9F9EFC16BCF1440950F41D5E8C747403FAF78EA91CE14400A4D124BCAC7474014D9BDCBFBCE1440DEB87B6FC2C7474028501FDCF8CE1440E54DD944C1C747409159075108CE144093F30F6CA6C74740C22DC48FD6CD14400054162AA4C74740B3A2618CA3CD14407B185A9D9CC747403E659016C2CD144052FB08579BC74740EDB4DA68A5CD14408E8FBBAD99C747403ED4111B87CD1440888153C48EC74740AAA1687979CD14402948C73082C7474043C70E2A71CD1440BE970C1181C74740CA3F8E4182CD1440B6357DD179C74740F4C7B4368DCD144052007B3B78C7474040D2F1E20ECD1440CA56F20C75C74740098F9147CBCC1440FA92D79475C74740BAD9C46E44CC14406231EA5A7BC74740C4D4963AC8CB1440852FA75F7DC74740950D6B2A8BCA1440132C0E677EC74740F24B58761CCA1440C4CA0D2B81C74740554A743BA0C91440816DB36785C74740370D9B125AC8144073A25D8594C74740350AEEAC82C51440E40DD539ABC74740F908466F02C51440E02AF46679C747406AE78FC426C51440286728EE78C74740553772384EC51440189D876E51C74740AE19749DA1C31440A7C7009A52C74740316FC44950C2144012F4BCC051C74740F6AFF6C143C2144081E5AD5F55C74740A6892DF30CC01440ACE39DE85FC747407049699148BE1440290EFB986AC7474062B4D83741BD144093A4106D6CC74740666E19CB4FBD144016BAB7C777C7474087B0D0BD3DBE1440CF97288C77C74740ECE7A8FE9CBD1440E3885A3FA2C74740A139A1C673BC1440D5676215CAC74740A8604326CFBB14402409675CDDC74740D924E428E5BA1440E847687000C847408408821145BA14408D4F5CE910C847400BEB6B02B1B914409FF7109E1AC84740743F4C67CCB91440DFAC1CB51DC84740DEF016A309B714406977483140C84740A4A60293C0B31440B21CD7755EC8474030325C78B9B31440004A8D1A5DC847400EA72787AAB31440123FB5B05DC847403A7CD28904B314407DE47BEB68C847409A43520B25B3144096664EF27EC84740C1B057B329B21440CFD1F4238FC84740CA49720A3CB11440EDB4DA68A5C84740B720A523CAB01440E75608ABB1C84740DD4834DCDBB01440EBBF18DBB5C847401B4EF454E2AD144000FF3971DEC847406425E65949AB144088FE64E7C8C94740BA86191A4FAC1440B79FE7AAD4C9474054CC9C8944AC144016D0FA4AD6C94740835C983BE9AB144020589FBCD9C9474011C1DDFE01AB144048F6AD7BD0C94740EE5F596952AA1440476CC19CEAC94740AD67AD0DBAA91440A3EFC91EFCC94740D99F1FA1C1A91440BA3FCD7FFEC94740FB0967B796A9144080E0E0C105CA47402CA4575E4DA9144011960C5B0ECA47400334000AE4A814409165C1C41FCA4740994FB1C5C9A814405CC8C8FE1ECA47403B02B859BCA8144096AC2FB720CA47402E3F709527A81440DC10887C3CCA47405A30F14751A71440E006C60B33CA47408DC8C1B68FA61440F9D9C87553CA4740079ACFB9DBA51440B5CF19074DCA4740E68DDD4FD7A41440E27A14AE47CA47400300112C58A41440A08B868C47CA4740847C75FAE6A314408609FE124CCA4740838D469968A31440AA5E23EE56CA474065D12AE917A314405F04D7265FCA4740CE5BD0D61CA31440ABF70AC160CA47402D2C13D962A21440D27BBEC172CA4740D3307C444CA1144086E2337E86CA474008A3A36659A114404F7B4ACE89CA4740D88BFC55DBA0144098569E9B91CA47404043499231A01440CA4054D0A3CA4740A2F48590F39E14401A33897AC1CA4740233B25D6989E1440855C4EAECECA47405EA7EC4F3D9D14405D7347FFCBCA474014ED2AA4FC9C1440FE14223BCACA474026F8F076CE98144098EAB8D04BCB4740FA916CBFC6971440A95E6D7B71CB474003ECA35357961440DF7B6E579ECB4740607E0283FF9014409E3358271DCC4740097250C24C8B144025DA441CA1CC47409EA7F0564C8B144029E73004A5CC4740C81231804B8B14404266C28AAECC4740BDEE63BB318B144089580E3EDECC47407CB5487FD48B14400AEC7B1EEDCC4740ECCD5E6C108C1440F4114251EACC47401A8057152B8C14406816574BF0CC4740D6A7C1D9528C14404867052BF3CC4740D31ADE077B8E14402FA3FDB4F6CC474000259930F58E1440F7216FB9FACC4740A0DDC60B8E8F144094E1783E03CD47405B0641ECF18F144024B149230BCD47400ED30847DA8F144081A7DAB80FCD4740C17630629F9014406D33BA281FCD47400427367A90911440B6ACB13A28CD47402D2C13D962921440021077F52ACD474056212FB5EF93144031D692E92BCD47407B53ECC3D5941440C76D8F392ACD4740E4F2C418FD951440E4AE14B82BCD4740F3BC0ADFB1961440EAFBBA1D2BCD4740C637143E5B971440286893C327CD474010EA2285B2981440B5E8F8C32ACD4740F2D3B837BF991440F5948BE722CD4740AE5AE37D669A1440E38BF67821CD4740FA556FC3DE9A14408C0AE6F91DCD4740B4C5DA84209C1440C5FC811722CD47403C6E5397E79C144054E57B4622CD4740606F078FDB9C14408BEA083B20CD4740D9DAB1C7FA9C1440A452EC681CCD4740AD0617E1DC9D144034805C870FCD474046C1429C3D9E144018B8978FFFCC4740EF8E311C749E14402C499EEBFBCC474056698B6B7C9E1440E6A210B7F9CC4740BF7F9829529E1440E110AAD4ECCC474054CD075FF39D14403D122F4FE7CC4740747FF5B86F9D14400953944BE3CC4740D08C34A7819D1440CB4DD4D2DCCC47400032BE79BB9F14402786E464E2CC47406849DB53CD9F14408E1546C4DECC474090DF36AEDAA01440AC79E981EACC47403EA0223DFBA0144021393EFFE2CC4740188DC6EB0BA2144025462AE7E6CC4740666EBE11DDA3144013578451FFCC4740B97768B345A51440CDA5023807CD4740CA2AB693D2A51440E878BB140DCD4740C2E3367579A61440481ADCD616CD474005BDDCCC8DA61440F07E260C14CD4740F17096ED9EA714401D3B4D0B03CD4740D7B09586D0A71440029F1F4608CD47400E805DF2E4A71440686AC82E07CD474025B20FB22CA8144099147A5803CD474061600C9BB7A814400C83E8FF0BCD4740749DA1139DA81440DA907F6610CD4740E2B3D02923A91440289364671BCD4740C9D2E2E71AA9144072D014F021CD47407E70E3BB39A91440E8CC2CF823CD4740D8416F3B7EA91440A321E3512ACD474057E1E01CBFA9144099D0C9F731CD47408A86D6D4C3A91440CE0B660234CD47409EE05119B5A91440A42A5C4535CD4740DDBCCC0B0BA914405333B5B63ACD47400848EAF307A91440D681621A3CCD47407108B02369AA14406E49B31E63CD4740EA25C632FDAA14403F4459AE6DCD4740500537AD6FAB1440B406EFAB72CD47405B00643266AC1440984AE4277ACD474053CDACA580AC1440324BF1A77CCD47407B53ECC3D5AC14409A0F632D99CD47407C04487E0EAD1440AA46544DB5CD47405636621C12AD1440D6E0229CBBCD474053454CE4DDAC1440730289DCC2CD4740C680368309AD1440757396FECACD4740AD16D86322AD144044BBAF6FCCCD47404FACF82B09AD1440E07DFA74E1CD474001F676F0B8AD14408BB5AE87E5CD4740C5A63A89ADAD1440583F918202CE4740FFB5BC72BDAD1440ABE408BE0ECE47404053AF5B04AE1440D0CCDDF824CE4740AA79D85592AE1440BE3F941E3CCE4740BA6702EA72AF14405958CBF852CE474046D6750360AF1440FF4E498C54CE474069965F611CAF144025C742194FCE4740E049B0DDE2AE1440F9382E3E60CE4740D28B7F44B8AE14401F44D72A66CE474081E37CFB84AD144086C88E3287CE4740C99640A5A5AD1440A203DC3D8ACE4740D11B936A44AD1440230D13B298CE474020A9746671AD1440434F1432A1CE4740BCF4E5BB4AAD144040654689A7CE4740517D31A53FAD144018E71489AECE47404520A8644AAD14403799A729B8CE4740B27D6DA23BAD1440B929D489BACE4740DAA4EC3E11AD1440AC47D04EBCCE4740E668441FE2AD1440D932CF00CDCE4740B42A1D07B9AE14402B76EA80DACE4740CD7B9C69C2AE1440A777F17EDCCE4740FC1BB4571FAF144010441669E2CE4740F3AC495C6CAF14408B598A3FE5CE47401A33897AC1AF14405E7A4501ECCE4740205498ADABB01440FD0CB963FBCE4740E94A04AA7FB01440FF4FB46103CF4740EE07E1760DAF1440F89D70C0F8CE47407A347ADA86AE1440CE4CD5E2F8CE474009C95DDFE2AC1440ADE4BE30F4CE47404AA8CFC42AAC14408EC5DBEFF5CE4740EA57DF652BAC1440EB45A3F1FACE4740FF4701FD19A91440CCB3379D09CF47408FC70C54C6A714409C01898109CF4740791CAB39E5A61440F621B94615CF4740B7A79A0FBEA614405CC8C8FE1ECF4740929735B1C0A714404F6848CB37CF474090B234A616A81440FCE82F4F42CF47406E1F4D501EA9144081E5AD5F55CF4740C6B4256195A914409944622761CF4740E0A52FDF55AA14408CAD56DC6ECF4740BA0155922BAB1440B0027CB779CF474031B9AC1D20AB1440D17EFF417FCF474014D3E01170AB14402CC5443987CF4740A8CBBD1B66AB1440D69A9C908CCF4740B11A4B581BAB144078C6AD388BCF474006476EF200AB1440D382BCC392CF47407080F4A853A91440B62792F991CF474058A2FD593DA91440187F36CD96CF4740CBE9A16C80A814402307DB3E9ACF4740CB8EE8F92EA814403C55192197CF4740B563343C07A814404E92F82697CF4740A1377062A3A714404544D6BF90CF4740E449777C20A614406CEAE1708BCF4740A4FD0FB056A51440540438BD8BCF4740057E9E5099A41440743804D891CF474033CDCF6806A414404533AA679CCF474051BA9976D6A314402BF3A0EAA1CF4740798A66AF88A31440ADBA5862AFCF4740E827E66A75A314402336B3F1BBCF4740708D19F219A214406B33A9FCC6CF47403AEAE8B81AA11440FA241C1FD2CF47408FF4B3A2179F144025ABC722F2CF47403845ECB8869E14406C515557F4CF47408D4C1B69049E14402C5D0BC4FCCF474024B6604E759D144069931EE10FD04740855BE3D81F9D1440FA23B14923D047403E0D73DDEF9B14403774B33F50D04740B96EEFBF3A9B14403C2C79E173D0474036142D2FAF9A1440328E36339FD0474051A15F00869A1440F6285C8FC2D04740514EB4AB909A144062026D61CCD0474095EC7D4F519A1440A20BEA5BE6D0474087A2E5E5559A1440BE5DE5BFF6D047402B57D350FE99144085285FD042D147400EF96706F1991440A2FFD42E5CD14740F0D1D160099A1440CC2BD7DB66D1474029C8748D3B9A144086EAE6E26FD147400EA320787C9B144055F6025889D14740DB3F0576EB9B144097EE096D94D1474018659181979C1440268458479FD147401C2CF75D6C9D1440AAE7EE84A8D14740119DAFA3BB9D14406CA3F08FADD1474000A13F7E809F144072BD12EDCFD14740595AFC5C23A014408027D2A2E3D147406C2409C215A014409052094FE8D147409C447353B99F1440DAC3036FEDD1474040016FDCBD9F144045D0E2D6EED147408B6C8C4237A01440AA1496D3F9D147400F08196DFAA01440D23131A715D24740B89B3B5519A11440191241E614D24740B4D5404EF3A11440C37A489D25D247402F22403C01A3144080153B7540D24740A1788489F5A41440A8F397CC67D247405F375B2F3CA51440F00A332372D24740073CD5C67DA81440C879A466B4D247406AE9C028F7A914405A057B5DD0D24740AFC44D57C1A914401D5DA5BBEBD247409A0D32C9C8A914409844076EEED24740214322C89CAA1440AB347392F7D2474069B16F82CAAB1440364A4D710BD34740512E32A605AC1440304ED76E16D34740527C218903AD1440190DCF0138D347406B0F7BA180AD1440CD086F0F42D3474013245B0295AE144093C9A99D61D34740B6B5CF74C0AF14404E0D349F73D3474004858B2661B014409354A69883D347400E011B6BDAB0144092B64CE19CD34740E76D11BD31B1144068203B14AAD34740B08971A36DB1144002DFC897ABD34740B05CCA541CB21440093A5AD592D34740AE7BD058A0B21440F4D8E08E81D34740C3482F6AF7B3144073B8FBC165D34740A4816962CBB41440C10A4B9759D34740118134AD5EB714408C61985D41D3474058C7968A32B814402A2158552FD34740600A6A53D0B81440C9B08A3732D347401E88D11852BA144051D6146D33D34740F038A000E5BA1440DB7DD81C31D34740729989D816BC1440B85F3E5931D347403FECE0BB28BD14402D42B11534D347401FE85729F3BD1440F21593ED32D347404E017A2B5CC01440BA2F67B62BD347405F20521DBCC01440095D1D5B2AD347409ED32CD0EEC01440FF36D5A425D3474048F2A66CA2C01440A9B8BB1814D347406730A1DD7CBE14402A66738BAFD24740912342C81EBE1440A6A3778599D24740B2BB404981BD144010B1C1C249D2474091A3946B65BD14403DBD529621D24740814EF7F01BBD1440B1CF52680FD24740FB0A2D46B8BC144007D15AD1E6D14740A36021CE1EBB144015843CCCA8D14740DD30C0F4ACBA1440F68079C894D147409E7DE5417ABA144045CBCBAB84D147407855568968BA144006F6984869D14740408BA548BEBA1440C001D2A34ED14740C744EFF906BB144033079C001ED1474013DF3FCC14B9144075D42C2BA8D04740C24362CCA7B81440234097BA75D04740D58DC1D4E0B7144076429E1331D0474080796DDB9CB714402DB4739A05D04740165454FD4AB7144084B064D872CF474020B6F468AAB714405CD8898164CF47408FF92587F4B81440D419CEEB3ECF4740CE678A4A34BA14404D6C3EAE0DCF47409ED497A59DBA1440A6A20CB0EACE47401C8AF150CABA14404F5DF92CCFCE4740442D173B2BBB14400DFD135CACCE4740F0B03F3F42BB14409DBD33DAAACE47407A7077D66EBB1440F1AFD46993CE474016A17D5127BD14400DA9A27895CE4740E3772EE7ADBD144056BDFC4E93CE4740CB9D3EA7C5BD1440B84DA72B8ECE4740CA54C1A8A4BE1440DFBAACD392CE4740514CDE0033BF144006E0FA0B98CE47407BA35698BEBF1440F9CFE4AC99CE474060ED8387C4C01440FB99D59695CE4740D111AFA18AC1144058456E3C8ECE474057DE44E33FC31440738577B988CE474028F38FBE49C31440C9E6AA798ECE47404CF9B59A1AC514408F3056E58CCE4740B6D5517A4BC614403B4668BA8DCE4740214E716605C81440B41B221395CE474022AD31E884C81440B884324399CE47404F475BF0FDC8144093043CC49ACE4740D5586721A6C91440E78CCD339BCE4740C2189128B4CC1440D39C610F92CE47402176A6D079CD14407443F8BC87CE474010D48448E1CD1440ADA2E47A80CE4740083B6AF1CECE1440DC0022B369CE4740CF58EA0F83CF1440B2135E8253CE47400B28D4D347D01440AC39403047CE474089DA9145F5D114402A86F5903ACE47404757E9EE3AD3144031A248522DCE474092C20655B4D3144064E2A0CE26CE4740B82D80DF75D4144072F5AD2017CE4740A2A476757DD51440145A7B44F1CD47401CDE6234E1D5144084A10E2BDCCD474026C1C01836D71440F3EA1C03B2CD4740DE859CAD72D71440C527F801A3CD47407128D76F81D714407168DB7A97CD47402FE7AD15C8D71440BBDB508C87CD4740F49F7FCC18D814403EB72B4F7BCD4740A9EAD44B42D81440A0138C9477CD4740524832AB77D8144005DF347D76CD4740357F4C6BD3D81440BCD80F0C7BCD474066016E71E8D814402705CC327ACD4740C1A442869FD914402491C71D80CD474055E0641BB8DB14408C2FDAE385CD4740673BF07BE5DB1440BCC4A2337ACD4740E91615CCF3DB1440A4D57F8C6FCD4740A6EFDA4A54DC14400FA0DFF76FCD47404E7FF62345DC14400F23298EA8CD474017DBA4A2B1DE14401C86EA419CCD47401C4BB3D4D5E014401E30B4DF7FCD47401250E10852E11440CDC5843D92CD47408D6BD7DF6DE114403BED84F299CD47408B9EA57565E114405FA40689A3CD474002FB438E08E11440261708A7AACD4740F9DD74CB0EE1144090C18A53ADCD4740844DF80038E1144003081F4AB4CD4740C6B01E5267E1144016319177B3CD47403FE2FCF26EE11440B66AD784B4CD4740B547B945AAE1144061634D1BC4CD4740087A032736E214403F07F1DCD6CD47404998B38872E31440E8082AF40BCE47404186E9D6C6E314409A85D10726CE4740F648DE944DE414406C6F01B221CE4740C6B5EBEFB6E414402BD1A3F32BCE47405D9C42F861E61440C26966D24BCE474022307B7EBDE71440D576B8C260CE47400E2EC2B92BE81440879A32816CCE47407801405FC4E814402A7C11C880CE4740921040C5CCE9144080E6BD7B91CE47405F984C158CEA1440D54FEE2D9BCE47402E5D1CF054EB14400012972DA3CE4740AAFCC63258EC14402241A7D6B1CE47409616E41D96EC1440488556CCADCE47403E72B55F88EE14408B2AB28CC3CE4740437EECE4C2EF14406E9681B9CCCE47406D55B71F95EF144051FC1873D7CE47404F475BF0FDF014406883A7EBE4CE474066587B8E7EF21440C85B53C5F9CE4740E47D665AACF214404008B76BF8CE47403A67FADB54F314401033EA10EECE47402003C30314F41440B356FEC6E8CE4740EB84E16DEFF4144077A565A4DECE4740A007D22060F81440E4C8B9032BCF47407D92E0B2AFF91440C1FE902342CF47406D776A89F0FA1440D80462235ACF4740A741D13C80FD1440A78988AC7FCF4740D72027A7D1FF1440E979DC5CA1CF4740EBA9D55757051540CD7B9C69C2CF47405A9E077767051540D9857A55C2CF47400722307B7E05154052B41776BDCF47406759411DA00515406511D43ABBCF4740C36856B60F091540C3B92B60A7CF474058676B3304061540C04B040539CF4740CD5944B9D904154009991A5712CF4740F47F5880000315405219B5B1C8CE47407B747EE59CFB144009E7AE809DCD4740D43C9171D6F91440A046109D54CD47408529CAA5F1F314409473BDC85FCC4740B066536463F41440A4B5584057CC47408E0EA37B31F714406E3C331C2ACC4740EEFF2D1224F81440311D84CA09CC4740D0543EBAC7F814405C71169BFBCB47407EA2FCEE67FA1440AFE13323CDCB4740102ED3E583FC1440E0C95D3A9CCB47400800338408FD144041A77BF88DCB47407781374998FE1440223999B855CB474036D07CCEDDFE14405F5A796E46CB474007CCE8EC09FF1440A926D2472ACB47401E82F45EC6FE14405680947DFCCA4740B5673B4B3500154066A208A9DBCA47401028F62BF80015408E9EB6A1BDCA474036DDFCD0160115400E7C56E3B6CA47402DC02D0E1D0115404ECCC4BEAECA474007240626DC001540F515A4198BCA474039A91E1FE30015405223997A82CA47409ABE8D83010215401E77A5C05DCA47402A92AF0452021540BE0056A247CA474008F7B990FD0215401A2433CD2ACA4740D5022093310315408DCE9E701BCA4740898043A852031540E0162CD505CA4740EC0FDE6811031540EFFBDC75DBC94740A8AED74F5A031540749EB12FD9C9474001A370E250041540E665039FD5C94740543D997FF4051540360FBB4AD2C94740B4B684D7890615403EBE73DEB5C947403BDC589AA506154000B03A72A4C94740774B72C0AE0615409722540493C94740F991110654061540072AE3DF67C94740884446AC6A061540377D2C335BC947404D6DA983BC06154049CB37914FC94740DC87BCE5EA071540C703801942C94740A9A04731E50815403C95C2723AC9474007DA780B7F0915402FB07DB72FC94740C9A1348A9B091540F6A974C12AC9474076D2B139BD0915407B771A1F1CC94740961D2CADD00915407431618FE4C8474083781332460A1540E790D442C9C8474020E97871870A15406EC1525DC0C84740844A5CC7B80A154069435953B4C847401BA8E738120B15404A4F47B6A9C8474000C80913460B154027A435069DC847402201A3CB9B0B154084CD4AA47EC8474064A428E1530A1540676FDF5971C847409FFC27C984091540935C59FD6CC84740541D7233DC08154032F77FC465C84740633DFF8705081540BB4D131159C847401211A38C5D07154041CE458D53C84740BB83D89942071540EC7BC33357C84740AE69DE718A061540696F95CC56C84740E2293F04B0041540D2A34EE559C84740B3C6EAA05C04154036F570B845C847404D4233993B0415406C5ED5592DC847407D66A4390D04154077FDDD9623C84740872FB88BD5031540441669E21DC847400C46802E7503154053A8B8BB18C8474053509B824602154009E1D1C611C84740838DEBDFF50115400FB8AE9811C8474077F69507E901154010ECF82F10C847403B866CC5A3011540B2DAFCBFEAC74740AD10FBA99601154085A7469EDAC7474089073994460115400FA8ED15CCC747409B6E7E688B0015400FE4EA11B4C7474060E16E5A3A001540CB2CE7F7A2C747401C3A877CE1FF1440C4B068DF81C74740C781FC112B001540AEA81CEE7EC74740DCDD5E2DD2FF1440672728EA71C74740E650D037B3FF144054234FED67C747407E31A53F45FF1440038A479858C74740");
    	city1.setShape(shape);
    	city1.setMunicipality(false);
    	this.cityDao.save(city1);
    	
    	String shapeAsWKT = this.cityDao.getShapeAsWKTByFeatureId(null);
    	Assert.assertNull(shapeAsWKT);
    	
    	
    	shapeAsWKT = this.cityDao.getShapeAsWKTByFeatureId(city1.getFeatureId());
    	Assert.assertEquals("POLYGON((5.2492876 47.5573912,5.2487588 47.556832,5.2488413 47.5567717,5.2488805 47.5558618,5.248781 47.5552611,5.2485969 47.5549663,5.2484059 47.554806,5.2479025 47.5545526,5.2477926 47.5550879,5.2468564 47.5555131,5.2449987 47.5560455,5.2434337 47.556651,5.2405684 47.5573809,5.2374841 47.5582846,5.2367758 47.5585522,5.2355591 47.5588169,5.2334882 47.5594377,5.2288021 47.5611833,5.2273719 47.5620323,5.2251694 47.5626609,5.2242348 47.5631048,5.2217021 47.5635101,5.220418 47.5635567,5.2208048 47.5639987,5.2206452 47.5641321,5.2212229 47.5646671,5.2215208 47.5650706,5.2156228 47.5674613,5.2154766 47.5674855,5.2127939 47.5689165,5.2122474 47.5681546,5.2120839 47.5682141,5.2117272 47.5678654,5.2107047 47.5670684,5.2106981 47.567004,5.2098244 47.5665295,5.2089421 47.5658496,5.2083513 47.5662685,5.2080465 47.5660405,5.2078043 47.5660894,5.2076621 47.5662695,5.2076457 47.5662802,5.2073796 47.566422,5.2068508 47.5669748,5.2068065 47.5670211,5.2062679 47.5672329,5.2054342 47.5664711,5.2057028 47.5662834,5.2053553 47.5659445,5.2046928 47.5662462,5.2044681 47.5664502,5.2043732 47.5665731,5.2041197 47.5664848,5.2040807 47.5665184,5.2036196 47.5661917,5.203438 47.5659604,5.2027206 47.5662729,5.2019988 47.5652776,5.2018713 47.5652213,5.2010108 47.5642499,5.2016854 47.563953,5.2007004 47.5628653,5.201045 47.5626959,5.20091 47.562531,5.2025595 47.561793,5.2017285 47.560861,5.2021324 47.5606212,5.2021212 47.5605856,5.2012036 47.5597663,5.2010138 47.5596974,5.2008192 47.559467,5.2009357 47.5594281,5.2008263 47.5593774,5.2007107 47.5590444,5.2006587 47.5586606,5.200627 47.5586263,5.2006922 47.5584051,5.200734 47.5583567,5.2002521 47.5582596,5.1999942 47.5582758,5.1994798 47.558452,5.199006 47.5585136,5.1977965 47.558545,5.1973742 47.5586294,5.1969003 47.5587587,5.1956561 47.55922,5.1928813 47.5599129,5.1923921 47.5583924,5.1925307 47.558378,5.1926812 47.5571726,5.1910462 47.5572083,5.1897594 47.5571824,5.1897116 47.5572929,5.1875494 47.5576144,5.1858237 47.5579406,5.1848191 47.5579964,5.1848747 47.5583429,5.1857824 47.5583358,5.1851692 47.5596389,5.1840354 47.5608546,5.1834074 47.5614429,5.1825148 47.5625134,5.1819041 47.5630161,5.1813393 47.5633123,5.1814438 47.5634066,5.1787477 47.564459,5.1755393 47.5653827,5.1755122 47.5653413,5.1754552 47.5653592,5.174822 47.5657019,5.174946 47.5663741,5.1739872 47.5668683,5.1730806 47.5675479,5.1726461 47.567922,5.1727137 47.5680498,5.1698087 47.5692884,5.167272 47.5764436,5.1682705 47.5768026,5.1682302 47.5768522,5.1678819 47.5769573,5.1669998 47.5766749,5.16633 47.5774723,5.1657488 47.5780066,5.1657777 47.5780792,5.165614 47.5783007,5.1653342 47.5785631,5.1649324 47.5790945,5.1648322 47.5790709,5.164781 47.5791234,5.1642135 47.5799709,5.163396 47.5796828,5.1626576 47.580672,5.161971 47.5804757,5.1609776 47.5803125,5.1604926 47.5803085,5.1600608 47.5804466,5.1595787 47.5807779,5.1592709 47.5810288,5.1592897 47.5810777,5.1585802 47.5816271,5.1575175 47.5822294,5.1575676 47.5823305,5.1570867 47.5825686,5.1564391 47.5831242,5.155226 47.5840295,5.1548799 47.5844324,5.1535542 47.5843505,5.1533075 47.5842966,5.1492251 47.5882512,5.1482191 47.5894007,5.1468175 47.5907697,5.1415997 47.5946397,5.136035 47.5986667,5.1360334 47.5987859,5.1360302 47.5990766,5.1359319 47.6005323,5.1365528 47.6009863,5.1367814 47.6009008,5.1368831 47.6010832,5.1370348 47.6011709,5.1391412 47.6012789,5.1396072 47.6014015,5.1401903 47.6016615,5.1405713 47.6019024,5.1404811 47.6020423,5.141233 47.6025134,5.1421527 47.6027902,5.1429552 47.6028735,5.1444691 47.6029026,5.1453467 47.6028511,5.1464733 47.6028967,5.1471629 47.6028783,5.147809 47.602776,5.1491185 47.6028676,5.1501435 47.6026277,5.1507816 47.602584,5.1512404 47.6024773,5.1524678 47.6026029,5.1532272 47.6026085,5.1531813 47.6025461,5.1533004 47.6024295,5.1541629 47.6020364,5.1545319 47.6015491,5.1547398 47.601438,5.1547715 47.6013707,5.1546103 47.6009775,5.1542487 47.600809,5.1537465 47.6006865,5.1538149 47.600489,5.1559886 47.600659,5.1560567 47.6005483,5.1570842 47.6009066,5.1572084 47.6006774,5.1582486 47.6007966,5.160023 47.6015417,5.1613987 47.6017828,5.1619361 47.6019617,5.1625727 47.6022595,5.1626503 47.6021743,5.1636922 47.6016554,5.1638814 47.601815,5.1639593 47.6017817,5.164233 47.6016646,5.1647629 47.6019287,5.1646617 47.602063,5.1651732 47.6023988,5.1651417 47.6025982,5.1652593 47.6026602,5.1655206 47.602854,5.1657681 47.6030874,5.1657861 47.6031497,5.1657299 47.6031882,5.1650812 47.6033543,5.1650694 47.6033967,5.1664167 47.6045874,5.1669815 47.6049097,5.1674182 47.605062,5.1683586 47.6052904,5.1684595 47.6053667,5.1687842 47.6062371,5.1690006 47.6070954,5.1690144 47.6072879,5.1688152 47.6075092,5.1689816 47.6077574,5.1690765 47.6078014,5.1689803 47.6084429,5.1696508 47.6085672,5.1696073 47.6094516,5.169668 47.6098249,5.1699385 47.6105033,5.1704801 47.6112097,5.1713368 47.6119071,5.1712647 47.6119552,5.1710067 47.6117889,5.1707873 47.6123121,5.1706248 47.6124929,5.1694526 47.6135009,5.1695772 47.6135938,5.1692063 47.6140349,5.1693779 47.6142943,5.1692304 47.6144878,5.1691881 47.6147014,5.1692291 47.6149952,5.1691728 47.6150677,5.1690111 47.6151217,5.1698079 47.6156312,5.1706277 47.6160432,5.1706635 47.616104,5.171018 47.6162845,5.1713118 47.6163711,5.1716365 47.6165773,5.1725299 47.6170468,5.172362 47.6172907,5.1709498 47.6169663,5.1704363 47.6169704,5.1688342 47.6168271,5.1681319 47.6168804,5.1681343 47.6170332,5.1651382 47.6174809,5.1638425 47.6174776,5.1629838 47.6178368,5.1628344 47.6181334,5.163821 47.6188902,5.1641489 47.6192111,5.1651547 47.6197929,5.1656089 47.6201524,5.1663432 47.6205707,5.1671584 47.620902,5.1671147 47.6210711,5.1674197 47.6213142,5.1673817 47.6214772,5.1670965 47.6214362,5.1669958 47.6216664,5.1653582 47.6216423,5.1652731 47.6217896,5.1645524 47.6218947,5.1642417 47.6217996,5.1640901 47.6218003,5.1637092 47.6216049,5.1622333 47.6214429,5.1614635 47.621452,5.1607411 47.6216383,5.1601807 47.6219606,5.1599978 47.6221288,5.1597011 47.6225398,5.1596276 47.6229231,5.1583021 47.6232601,5.1573285 47.6235999,5.1553636 47.6245769,5.1548108 47.6246442,5.1543137 47.6249013,5.1537678 47.6254846,5.1534418 47.6260769,5.1522822 47.627449,5.1515913 47.6285364,5.1510589 47.6298584,5.1509018 47.6309375,5.1509425 47.6312372,5.1507008 47.63203,5.1507183 47.6325302,5.1503842 47.6348515,5.1503335 47.6356257,5.1504264 47.6359515,5.1506178 47.636227,5.151842 47.6370039,5.1522654 47.6373421,5.1529217 47.6376733,5.1537337 47.6379553,5.1540361 47.6381092,5.1557636 47.6391579,5.1563849 47.6397594,5.156333 47.639902,5.1559804 47.6400584,5.1559977 47.6401013,5.1564608 47.6404366,5.1572053 47.6412858,5.1573232 47.6412628,5.1581547 47.6417729,5.1591844 47.6425921,5.1610929 47.6437927,5.1613624 47.6441082,5.1645423 47.6461304,5.1659819 47.6469838,5.1657766 47.647819,5.165805 47.6479013,5.1666137 47.6481803,5.1677647 47.6487867,5.1679903 47.6491221,5.1689588 47.6501467,5.169436 47.6504535,5.1704903 47.6514165,5.1716326 47.651966,5.1722456 47.6524535,5.1727082 47.6532251,5.1730413 47.6536279,5.1732698 47.6536741,5.1739362 47.6529185,5.1744398 47.6523913,5.1757485 47.6515429,5.1765571 47.6511716,5.1790721 47.6504323,5.1798803 47.649882,5.1804822 47.64997,5.1819538 47.6500069,5.1825142 47.6499363,5.1836809 47.6499435,5.1847257 47.650027,5.1854979 47.6499917,5.1878516 47.6497715,5.1882176 47.6497301,5.188411 47.6495863,5.1881196 47.6490508,5.1860232 47.6459822,5.1856643 47.6453101,5.1850635 47.642876,5.1849572 47.64165,5.1846769 47.6410952,5.1842967 47.6398565,5.1827347 47.6379638,5.1823004 47.637353,5.182107 47.6368613,5.1820394 47.6360255,5.1823665 47.6352124,5.1826438 47.6337281,5.1807434 47.6301321,5.1803276 47.6285928,5.1795686 47.6264977,5.1793093 47.625171,5.178997 47.6206923,5.179361 47.6202547,5.1806203 47.6191077,5.1818401 47.617605,5.182242 47.6165371,5.1824124 47.6156975,5.1827821 47.614635,5.1828699 47.614589,5.18304 47.6138737,5.1847203 47.6139365,5.1852337 47.6138705,5.1853243 47.6137137,5.186175 47.6138558,5.186718 47.6140151,5.1872505 47.6140648,5.1882497 47.6139401,5.1890054 47.6137157,5.1906734 47.6135475,5.190711 47.613723,5.1924843 47.6136748,5.1936473 47.6137002,5.1953331 47.6139244,5.1958195 47.6140522,5.1962812 47.6140981,5.1969228 47.6141114,5.199906 47.6138324,5.20066 47.6135174,5.2010547 47.6132959,5.2019613 47.6126007,5.2026484 47.6119235,5.203399 47.6115475,5.2050372 47.6111623,5.2062795 47.6107581,5.2067426 47.6105593,5.2074809 47.6100808,5.2084864 47.6089254,5.2088669 47.6082815,5.2101673 47.606995,5.2103984 47.6065371,5.2104547 47.6061853,5.2107242 47.6056991,5.2110321 47.6053256,5.2111904 47.6052118,5.211394 47.6051785,5.211744 47.6053176,5.2118242 47.6052917,5.2125226 47.6054723,5.2145695 47.6056485,5.2147426 47.6052918,5.2147972 47.6049667,5.2151653 47.6049795,5.2151075 47.6067064,5.2174745 47.6063311,5.2195657 47.6054649,5.2200395 47.6060254,5.2201457 47.6062606,5.2201136 47.6065532,5.2197592 47.6067704,5.219783 47.606852,5.2199402 47.6070645,5.2201207 47.6070394,5.2201498 47.6070715,5.2203761 47.6075472,5.2209097 47.6081196,5.2221166 47.6097398,5.2224382 47.6105356,5.2229522 47.6104033,5.2233541 47.6107163,5.2249831 47.6116889,5.2263088 47.6123279,5.2267293 47.6126863,5.2273116 47.6133051,5.2283202 47.6138148,5.22905 47.6141107,5.2298162 47.6143548,5.2308052 47.6148022,5.2310414 47.6146789,5.2329421 47.6153427,5.2341419 47.6156227,5.2339673 47.61595,5.2353437 47.6163611,5.2368109 47.6169974,5.2369856 47.6169562,5.2376284 47.6166402,5.2383576 47.6164788,5.2391946 47.6161695,5.2425542 47.6185002,5.2438343 47.6192059,5.2450582 47.6199383,5.2475595 47.6210838,5.2498232 47.6221119,5.255216 47.6231205,5.2552775 47.6231181,5.2553653 47.6229694,5.2554936 47.6229013,5.258849 47.6222954,5.2558754 47.6189276,5.2547368 47.6177472,5.2529316 47.6154997,5.2457157 47.6063691,5.2439821 47.6041447,5.2382265 47.5966731,5.2386604 47.5964127,5.2413997 47.5950351,5.2423251 47.5940488,5.2429494 47.5936159,5.2445371 47.5921978,5.2465969 47.5907052,5.2471028 47.5902701,5.2486278 47.5885535,5.248893 47.5880869,5.2490613 47.5872278,5.2488036 47.5858304,5.2502033 47.5848285,5.2509467 47.5839121,5.2510636 47.5837063,5.2510874 47.5834578,5.2508398 47.58237,5.2508664 47.5821069,5.2519589 47.5809861,5.252266 47.5803111,5.2529204 47.5794312,5.2531188 47.5789624,5.253245 47.578303,5.2529961 47.5770099,5.2532742 47.5769405,5.2542148 47.5768317,5.2558155 47.5767301,5.2563852 47.5758627,5.2564911 47.575331,5.256526 47.5747991,5.2561799 47.5734825,5.2562663 47.5730957,5.2565785 47.5727407,5.257732 47.5723297,5.2586868 47.5720962,5.2592737 47.5717687,5.2593824 47.5716173,5.2595109 47.5711707,5.2595851 47.5694751,5.2600334 47.568642,5.2602823 47.5683705,5.2604705 47.5680031,5.2608117 47.5676792,5.2610095 47.567292,5.2613365 47.5663648,5.2600856 47.5659592,5.2592956 47.5658261,5.2586525 47.5656057,5.2578336 47.5652181,5.2571928 47.5650498,5.25709 47.5651612,5.2563875 47.5651489,5.2545777 47.5652434,5.2542596 47.5646277,5.2541336 47.563884,5.2539567 47.5635861,5.2537443 47.563412,5.2533767 47.5632548,5.2522221 47.5630425,5.2519145 47.563037,5.2518655 47.562994,5.2516013 47.5618515,5.2515513 47.5613592,5.2512458 47.5609157,5.2505318 47.5601828,5.2502226 47.5596609,5.2498836 47.5586509,5.2501643 47.5585611,5.2498252 47.5581639,5.2497071 47.5578591,5.2492876 47.5573912))", shapeAsWKT);
    }

    @Required
    public void setGisFeatureDao(IGisFeatureDao gisFeatureDao) {
	this.gisFeatureDao = gisFeatureDao;
    }

    @Required
    public void setCityDao(ICityDao cityDao) {
	this.cityDao = cityDao;
    }

    @Required
    public void setAdmDao(IAdmDao admDao) {
	this.admDao = admDao;
    }

    @Required
    public void setAlternateNameDao(IAlternateNameDao alternateNameDao) {
	this.alternateNameDao = alternateNameDao;
    }

    /**
     * @param countryDao
     *                the countryDao to set
     */
    @Required
    public void setCountryDao(ICountryDao countryDao) {
	this.countryDao = countryDao;
    }

}
