-- MySQL dump 10.13  Distrib 5.7.42, for Linux (x86_64)
--
-- Host: localhost    Database: wsms
-- ------------------------------------------------------
-- Server version	5.7.42-0ubuntu0.18.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `observation`
--

DROP TABLE IF EXISTS `observation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `observation` (
                               `id` int(11) NOT NULL AUTO_INCREMENT,
                               `timestamp` datetime DEFAULT NULL,
                               `value` int(11) NOT NULL,
                               `sensor` varchar(255) DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3898 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `observation`
--

LOCK TABLES `observation` WRITE;
/*!40000 ALTER TABLE `observation` DISABLE KEYS */;
INSERT INTO `observation` VALUES (2733,'2024-02-05 08:00:00',42,'13905'),(2734,'2024-02-05 08:15:00',41,'13905'),(2735,'2024-02-05 08:30:00',40,'13905'),(2736,'2024-02-05 08:45:00',42,'13905'),(2737,'2024-02-05 09:00:00',41,'13905'),(2738,'2024-02-05 09:15:00',45,'13905'),(2739,'2024-02-05 09:30:00',48,'13905'),(2740,'2024-02-05 09:45:00',47,'13905'),(2741,'2024-02-05 10:00:00',42,'13905'),(2742,'2024-02-05 10:15:00',39,'13905'),(2743,'2024-02-05 10:30:00',45,'13905'),(2744,'2024-02-05 10:45:00',43,'13905'),(2745,'2024-02-05 11:00:00',38,'13905'),(2746,'2024-02-05 11:15:00',40,'13905'),(2747,'2024-02-05 11:30:00',43,'13905'),(2748,'2024-02-05 11:45:00',41,'13905'),(2749,'2024-02-05 12:00:00',46,'13905'),(2750,'2024-02-05 12:15:00',46,'13905'),(2751,'2024-02-05 12:30:00',44,'13905'),(2752,'2024-02-05 12:45:00',45,'13905'),(2753,'2024-02-05 13:00:00',42,'13905'),(2754,'2024-02-05 13:15:00',47,'13905'),(2755,'2024-02-05 13:30:00',42,'13905'),(2756,'2024-02-05 13:45:00',38,'13905'),(2757,'2024-02-05 14:00:00',48,'13905'),(2758,'2024-02-05 14:15:00',53,'13905'),(2759,'2024-02-05 14:30:00',58,'13905'),(2760,'2024-02-05 14:45:00',56,'13905'),(2761,'2024-02-05 15:00:00',56,'13905'),(2762,'2024-02-05 15:15:00',60,'13905'),(2763,'2024-02-05 15:30:00',59,'13905'),(2764,'2024-02-05 15:45:00',56,'13905'),(2765,'2024-02-05 16:00:00',55,'13905'),(2766,'2024-02-05 16:15:00',56,'13905'),(2767,'2024-02-05 16:30:00',53,'13905'),(2768,'2024-02-05 16:45:00',51,'13905'),(2769,'2024-02-05 17:00:00',46,'13905'),(2770,'2024-02-05 17:15:00',50,'13905'),(2771,'2024-02-05 17:30:00',55,'13905'),(2772,'2024-02-05 17:45:00',51,'13905'),(2773,'2024-02-05 18:00:00',56,'13905'),(2774,'2024-02-05 18:15:00',58,'13905'),(2775,'2024-02-05 18:30:00',55,'13905'),(2776,'2024-02-05 18:45:00',54,'13905'),(2777,'2024-02-05 19:00:00',59,'13905'),(2778,'2024-02-05 19:15:00',54,'13905'),(2779,'2024-02-05 19:30:00',55,'13905'),(2780,'2024-02-05 19:45:00',52,'13905'),(2781,'2024-02-05 20:00:00',47,'13905'),(2782,'2024-02-05 20:15:00',42,'13905'),(2783,'2024-02-05 20:30:00',41,'13905'),(2784,'2024-02-05 20:45:00',41,'13905'),(2785,'2024-02-05 21:00:00',44,'13905'),(2786,'2024-02-05 21:15:00',47,'13905'),(2787,'2024-02-05 21:30:00',47,'13905'),(2788,'2024-02-05 21:45:00',51,'13905'),(2789,'2024-02-05 22:00:00',46,'13905'),(2790,'2024-02-05 22:15:00',44,'13905'),(2791,'2024-02-05 22:30:00',40,'13905'),(2792,'2024-02-05 08:00:00',22,'17954'),(2793,'2024-02-05 08:15:00',23,'17954'),(2794,'2024-02-05 08:30:00',24,'17954'),(2795,'2024-02-05 08:45:00',25,'17954'),(2796,'2024-02-05 09:00:00',24,'17954'),(2797,'2024-02-05 09:15:00',25,'17954'),(2798,'2024-02-05 09:30:00',26,'17954'),(2799,'2024-02-05 09:45:00',25,'17954'),(2800,'2024-02-05 10:00:00',24,'17954'),(2801,'2024-02-05 10:15:00',23,'17954'),(2802,'2024-02-05 10:30:00',22,'17954'),(2803,'2024-02-05 10:45:00',23,'17954'),(2804,'2024-02-05 11:00:00',22,'17954'),(2805,'2024-02-05 11:15:00',23,'17954'),(2806,'2024-02-05 11:30:00',24,'17954'),(2807,'2024-02-05 11:45:00',25,'17954'),(2808,'2024-02-05 12:00:00',24,'17954'),(2809,'2024-02-05 12:15:00',25,'17954'),(2810,'2024-02-05 12:30:00',24,'17954'),(2811,'2024-02-05 12:45:00',25,'17954'),(2812,'2024-02-05 13:00:00',26,'17954'),(2813,'2024-02-05 13:15:00',25,'17954'),(2814,'2024-02-05 13:30:00',24,'17954'),(2815,'2024-02-05 13:45:00',23,'17954'),(2816,'2024-02-05 14:00:00',22,'17954'),(2817,'2024-02-05 14:15:00',23,'17954'),(2818,'2024-02-05 14:30:00',24,'17954'),(2819,'2024-02-05 14:45:00',23,'17954'),(2820,'2024-02-05 15:00:00',22,'17954'),(2821,'2024-02-05 15:15:00',23,'17954'),(2822,'2024-02-05 15:30:00',24,'17954'),(2823,'2024-02-05 15:45:00',23,'17954'),(2824,'2024-02-05 16:00:00',24,'17954'),(2825,'2024-02-05 16:15:00',25,'17954'),(2826,'2024-02-05 16:30:00',26,'17954'),(2827,'2024-02-05 16:45:00',25,'17954'),(2828,'2024-02-05 17:00:00',24,'17954'),(2829,'2024-02-05 17:15:00',23,'17954'),(2830,'2024-02-05 17:30:00',22,'17954'),(2831,'2024-02-05 17:45:00',21,'17954'),(2832,'2024-02-05 18:00:00',20,'17954'),(2833,'2024-02-05 18:15:00',21,'17954'),(2834,'2024-02-05 18:30:00',20,'17954'),(2835,'2024-02-05 18:45:00',21,'17954'),(2836,'2024-02-05 19:00:00',22,'17954'),(2837,'2024-02-05 19:15:00',21,'17954'),(2838,'2024-02-05 19:30:00',22,'17954'),(2839,'2024-02-05 19:45:00',21,'17954'),(2840,'2024-02-05 20:00:00',22,'17954'),(2841,'2024-02-05 20:15:00',21,'17954'),(2842,'2024-02-05 20:30:00',20,'17954'),(2843,'2024-02-05 20:45:00',19,'17954'),(2844,'2024-02-05 21:00:00',18,'17954'),(2845,'2024-02-05 21:15:00',19,'17954'),(2846,'2024-02-05 21:30:00',20,'17954'),(2847,'2024-02-05 21:45:00',21,'17954'),(2848,'2024-02-05 22:00:00',22,'17954'),(2849,'2024-02-05 22:15:00',23,'17954'),(2850,'2024-02-05 22:30:00',24,'17954'),(2851,'2024-02-05 22:45:00',23,'17954'),(2852,'2024-02-05 23:00:00',24,'17954'),(2853,'2024-02-05 01:30:00',0,'58937'),(2854,'2024-02-05 02:00:00',0,'58937'),(2855,'2024-02-05 02:30:00',1,'58937'),(2856,'2024-02-05 03:00:00',1,'58937'),(2857,'2024-02-05 03:30:00',3,'58937'),(2858,'2024-02-05 04:00:00',5,'58937'),(2859,'2024-02-05 04:30:00',9,'58937'),(2860,'2024-02-05 05:00:00',14,'58937'),(2861,'2024-02-05 05:30:00',21,'58937'),(2862,'2024-02-05 06:00:00',32,'58937'),(2863,'2024-02-05 06:30:00',47,'58937'),(2864,'2024-02-05 07:00:00',67,'58937'),(2865,'2024-02-05 07:30:00',93,'58937'),(2866,'2024-02-05 08:00:00',124,'58937'),(2867,'2024-02-05 08:30:00',162,'58937'),(2868,'2024-02-05 09:00:00',205,'58937'),(2869,'2024-02-05 09:30:00',253,'58937'),(2870,'2024-02-05 10:00:00',303,'58937'),(2871,'2024-02-05 10:30:00',353,'58937'),(2872,'2024-02-05 11:00:00',400,'58937'),(2873,'2024-02-05 11:30:00',441,'58937'),(2874,'2024-02-05 12:00:00',472,'58937'),(2875,'2024-02-05 12:30:00',493,'58937'),(2876,'2024-02-05 13:00:00',500,'58937'),(2877,'2024-02-05 13:30:00',493,'58937'),(2878,'2024-02-05 14:00:00',472,'58937'),(2879,'2024-02-05 14:30:00',441,'58937'),(2880,'2024-02-05 15:00:00',400,'58937'),(2881,'2024-02-05 15:30:00',353,'58937'),(2882,'2024-02-05 16:00:00',303,'58937'),(2883,'2024-02-05 16:30:00',253,'58937'),(2884,'2024-02-05 17:00:00',205,'58937'),(2885,'2024-02-05 17:30:00',162,'58937'),(2886,'2024-02-05 18:00:00',124,'58937'),(2887,'2024-02-05 18:30:00',93,'58937'),(2888,'2024-02-05 19:00:00',67,'58937'),(2889,'2024-02-05 19:30:00',47,'58937'),(2890,'2024-02-05 20:00:00',32,'58937'),(2891,'2024-02-05 20:30:00',21,'58937'),(2892,'2024-02-05 21:00:00',14,'58937'),(2893,'2024-02-05 21:30:00',9,'58937'),(2894,'2024-02-05 22:00:00',5,'58937'),(2895,'2024-02-05 22:30:00',3,'58937'),(2896,'2024-02-05 23:00:00',1,'58937'),(2897,'2024-02-05 23:30:00',1,'58937'),(2898,'2024-02-06 00:00:00',0,'58937'),(2899,'2024-02-06 00:30:00',0,'58937'),(2900,'2024-02-06 01:00:00',0,'58937'),(2901,'2024-02-06 01:30:00',0,'58937'),(2902,'2024-02-06 02:00:00',0,'58937'),(2903,'2024-02-06 02:30:00',1,'58937'),(2904,'2024-02-06 03:00:00',1,'58937'),(2905,'2024-02-06 03:30:00',3,'58937'),(2906,'2024-02-06 04:00:00',5,'58937'),(2907,'2024-02-06 04:30:00',9,'58937'),(2908,'2024-02-06 05:00:00',14,'58937'),(2909,'2024-02-06 05:30:00',21,'58937'),(2910,'2024-02-06 06:00:00',32,'58937'),(2911,'2024-02-06 06:30:00',47,'58937'),(2912,'2024-02-06 07:00:00',67,'58937'),(2913,'2024-02-06 07:30:00',93,'58937'),(2914,'2024-02-06 08:00:00',124,'58937'),(2915,'2024-02-06 08:30:00',162,'58937'),(2916,'2024-02-06 09:00:00',205,'58937'),(3750,'2024-02-07 00:30:00',50,'23397'),(3751,'2024-02-07 01:00:00',200,'23397'),(3752,'2024-02-07 01:30:00',200,'23397'),(3753,'2024-02-07 02:00:00',200,'23397'),(3754,'2024-02-07 02:30:00',201,'23397'),(3755,'2024-02-07 03:00:00',201,'23397'),(3756,'2024-02-07 03:30:00',203,'23397'),(3757,'2024-02-07 04:00:00',205,'23397'),(3758,'2024-02-07 04:30:00',209,'23397'),(3759,'2024-02-07 05:00:00',214,'23397'),(3760,'2024-02-07 05:30:00',221,'23397'),(3761,'2024-02-07 06:00:00',232,'23397'),(3762,'2024-02-07 06:30:00',247,'23397'),(3763,'2024-02-07 07:00:00',267,'23397'),(3764,'2024-02-07 07:30:00',293,'23397'),(3765,'2024-02-07 08:00:00',324,'23397'),(3766,'2024-02-07 08:30:00',362,'23397'),(3767,'2024-02-07 09:00:00',405,'23397'),(3768,'2024-02-07 09:30:00',303,'23397'),(3769,'2024-02-07 10:00:00',353,'23397'),(3770,'2024-02-07 10:30:00',403,'23397'),(3771,'2024-02-07 11:00:00',450,'23397'),(3772,'2024-02-07 11:30:00',491,'23397'),(3773,'2024-02-07 12:00:00',522,'23397'),(3774,'2024-02-07 12:30:00',543,'23397'),(3775,'2024-02-07 13:00:00',550,'23397'),(3776,'2024-02-07 13:30:00',543,'23397'),(3777,'2024-02-07 14:00:00',522,'23397'),(3778,'2024-02-07 14:30:00',491,'23397'),(3779,'2024-02-07 15:00:00',450,'23397'),(3780,'2024-02-07 15:30:00',403,'23397'),(3781,'2024-02-07 16:00:00',353,'23397'),(3782,'2024-02-07 16:30:00',303,'23397'),(3783,'2024-02-07 17:00:00',255,'23397'),(3784,'2024-02-07 17:30:00',362,'23397'),(3785,'2024-02-07 18:00:00',324,'23397'),(3786,'2024-02-07 18:30:00',293,'23397'),(3787,'2024-02-07 19:00:00',267,'23397'),(3788,'2024-02-07 19:30:00',247,'23397'),(3789,'2024-02-07 20:00:00',232,'23397'),(3790,'2024-02-08 00:30:00',50,'23397'),(3791,'2024-02-08 01:00:00',200,'23397'),(3792,'2024-02-08 01:30:00',200,'23397'),(3793,'2024-02-08 02:00:00',200,'23397'),(3794,'2024-02-08 02:30:00',201,'23397'),(3795,'2024-02-08 03:00:00',201,'23397'),(3796,'2024-02-08 03:30:00',203,'23397'),(3797,'2024-02-08 04:00:00',205,'23397'),(3798,'2024-02-08 04:30:00',209,'23397'),(3799,'2024-02-08 05:00:00',214,'23397'),(3800,'2024-02-08 05:30:00',221,'23397'),(3801,'2024-02-08 06:00:00',232,'23397'),(3802,'2024-02-08 06:30:00',247,'23397'),(3803,'2024-02-08 07:00:00',267,'23397'),(3804,'2024-02-08 07:30:00',293,'23397'),(3805,'2024-02-08 08:00:00',324,'23397'),(3806,'2024-02-08 08:30:00',362,'23397'),(3807,'2024-02-08 09:00:00',405,'23397'),(3808,'2024-02-08 09:30:00',453,'23397'),(3809,'2024-02-08 10:00:00',353,'23397'),(3810,'2024-02-08 10:30:00',403,'23397'),(3811,'2024-02-08 11:00:00',450,'23397'),(3812,'2024-02-08 11:30:00',491,'23397'),(3813,'2024-02-08 12:00:00',522,'23397'),(3814,'2024-02-08 12:30:00',543,'23397'),(3815,'2024-02-08 13:00:00',550,'23397'),(3816,'2024-02-08 13:30:00',543,'23397'),(3817,'2024-02-08 14:00:00',522,'23397'),(3818,'2024-02-08 14:30:00',491,'23397'),(3819,'2024-02-08 15:00:00',450,'23397'),(3820,'2024-02-08 15:30:00',403,'23397'),(3821,'2024-02-08 16:00:00',353,'23397'),(3822,'2024-02-08 16:30:00',303,'23397'),(3823,'2024-02-08 17:00:00',255,'23397'),(3824,'2024-02-08 17:30:00',362,'23397'),(3825,'2024-02-08 18:00:00',324,'23397'),(3826,'2024-02-08 18:30:00',293,'23397'),(3827,'2024-02-08 19:00:00',267,'23397'),(3828,'2024-02-08 19:30:00',247,'23397'),(3829,'2024-02-08 20:00:00',232,'23397'),(3830,'2024-02-08 20:30:00',221,'23397'),(3831,'2024-02-08 21:00:00',214,'23397'),(3832,'2024-02-08 21:30:00',209,'23397'),(3833,'2024-02-08 22:00:00',205,'23397'),(3834,'2024-02-08 22:30:00',203,'23397'),(3835,'2024-02-08 23:00:00',201,'23397'),(3836,'2024-02-08 23:30:00',201,'23397');
/*!40000 ALTER TABLE `observation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sensor`
--

DROP TABLE IF EXISTS `sensor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sensor` (
                          `nodeId` varchar(255) DEFAULT NULL,
                          `dataType` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sensor`
--

LOCK TABLES `sensor` WRITE;
/*!40000 ALTER TABLE `sensor` DISABLE KEYS */;
INSERT INTO `sensor` VALUES ('58937','brightness'),('13905','humidity'),('fd00:0:0:0:f6ce:3642:927c:4622','temperature'),('23397','brightness'),('f4ce366a5b65','brightness');
/*!40000 ALTER TABLE `sensor` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-02-09  8:44:15